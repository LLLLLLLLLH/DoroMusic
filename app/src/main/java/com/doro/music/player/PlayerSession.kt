package com.doro.music.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.paging.PagingData
import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlaybackState
import com.doro.music.player.controller.MediaPlaybackController
import com.doro.music.player.controller.PlaybackController
import com.doro.music.player.model.PlayUiState
import com.doro.music.player.model.QueueSong
import com.doro.music.data.repo.QueueReadOps
import com.doro.music.data.repo.QueueWriteOps
import com.doro.music.player.service.PlayerService
import com.doro.music.data.datastore.PlayStateDataStore
import com.doro.music.player.controller.EngineEvent
import com.doro.music.player.model.PlayAction
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface PlayActionDispatcher {
    fun dispatch(action: PlayAction)
}

interface PlayStateObserver {
    val uiState: StateFlow<PlayUiState>
    val currentPositionMs: StateFlow<Long>
    val playQueue: Flow<PagingData<QueueSong>>
}

interface PlayerConnector {
    fun connect()
    fun disconnect()
}

interface PlayerAccessor {
    val controller: MediaController?
    val isConnected: StateFlow<Boolean>
}

class PlayerSession(
    private val context: Context,
    private val queueOps: QueueWriteOps,
    private val queueReadOps: QueueReadOps,
    private val stateDataStore: PlayStateDataStore
) : PlayActionDispatcher, PlayStateObserver, PlayerConnector, PlayerAccessor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val navigationMutex = Mutex()
    private var sessionJob: Job? = null

    private val playbackController: PlaybackController = MediaPlaybackController(this)

    private var future: ListenableFuture<MediaController>? = null

    private var mediaController: MediaController? = null
    override val controller: MediaController?
        get() = mediaController

    override val isConnected = MutableStateFlow(false)

    // ==================== PlayStateObserver：状态暴露 ====================

    override val uiState = MutableStateFlow(PlayUiState.Empty)

    override val currentPositionMs = MutableStateFlow(0L)

    override val playQueue: Flow<PagingData<QueueSong>>
        get() = queueReadOps.getPagedPlaybackQueue()

    // ==================== PlayActionDispatcher：指令分发 ====================

    override fun dispatch(action: PlayAction) {
        scope.launch { handleAction(action) }
    }

    // ==================== PlayerConnector：连接管理 ====================

    init {
        observeConnection()
    }

    override fun connect() {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        future = MediaController.Builder(context, sessionToken).buildAsync()
        future?.addListener({
            runCatching { future?.get() }
                .onSuccess {
                    mediaController = it
                    isConnected.value = true
                }
        }, MoreExecutors.directExecutor())
    }

    override fun disconnect() {
        isConnected.value = false
        mediaController?.release()
        future = null
        mediaController = null
    }

    // ==================== 连接状态订阅 ====================

    private fun observeConnection() {
        scope.launch {
            isConnected.collectLatest { connected ->
                if (connected) onConnected() else onDisconnected()
            }
        }
    }

    private fun onConnected() {
        sessionJob = scope.launch {
            playbackController.events.onEach(::handleEngineEvent).launchIn(this)
            restoreFromDatabase()
        }
    }

    private suspend fun onDisconnected() {
        stateDataStore.saveCurrentPosition(currentPositionMs.value)
        sessionJob?.cancel()
        sessionJob = null
    }

    // ==================== 动作处理 ====================

    private suspend fun handleAction(action: PlayAction) {
        when (action) {
            is PlayAction.Play -> handlePlay(action)
            is PlayAction.Next -> handleNext()
            is PlayAction.Prev -> handlePrev()
            is PlayAction.TogglePlay -> handleTogglePlay()
            is PlayAction.TogglePlayMode -> handleTogglePlayMode()
            is PlayAction.SeekTo -> handleSeekTo(action)
            is PlayAction.SeekToQueueItem -> handleSeekToQueueItem(action.queueId)
            is PlayAction.InsertSingle -> handleInsertSingle(action)
            is PlayAction.InsertGroup -> handleInsertGroup(action)
            is PlayAction.Remove -> handleRemoveFromQueue(action.queueId)
        }
    }

    private suspend fun handlePlay(action: PlayAction.Play) {
        val songIds = queueOps.resolveSongIds(action.playContext)
        if (songIds.isEmpty()) return
        val playMode = stateDataStore.playMode.first()
        val targetQueueId = queueOps.playNewQueue(songIds, action.songId, playMode) ?: return
        playSongWithPreload(targetQueueId, playMode, positionMs = 0L, startPlaying = true)
        savePlaybackState(targetQueueId, action.songId, 0L)
    }

    private suspend fun handleSeekToQueueItem(queueId: Long) {
        val playMode = stateDataStore.playMode.first()
        playSongWithPreload(queueId, playMode, positionMs = 0L, startPlaying = true)
        queueOps.getSongIdByQueueId(queueId)
            ?.let { savePlaybackState(queueId, it, 0L) }
    }

    private suspend fun handleNext() = navigationMutex.withLock {
        val currentQueueId = stateDataStore.currentQueueId.first()
        if (currentQueueId == 0L) {
            Log.w(TAG, "handleNext: no current queue id")
            return@withLock
        }
        val playMode = stateDataStore.playMode.first()
        val currentOrder = queueOps.getOrder(currentQueueId, playMode)
        if (currentOrder == null) {
            Log.w(TAG, "handleNext: order not found for queueId=$currentQueueId")
            return@withLock
        }
        val nextQueueId = queueOps.getNextQueueId(currentOrder, playMode)
            ?: queueOps.getFirstQueueId(playMode)
        if (nextQueueId == null) {
            Log.w(TAG, "handleNext: no next queue id available")
            return@withLock
        }
        if (playbackController.hasNext()) {
            playbackController.seekToNext()
        } else {
            playSongWithPreload(nextQueueId, playMode, positionMs = 0L, startPlaying = true)
        }
        val songId = queueOps.getSongIdByQueueId(nextQueueId) ?: return@withLock
        savePlaybackState(nextQueueId, songId, 0L)
        scope.launch { preloadNext(nextQueueId, playMode) }
    }

    private suspend fun handlePrev() = navigationMutex.withLock {
        val currentQueueId = stateDataStore.currentQueueId.first()
        if (currentQueueId == 0L) {
            Log.w(TAG, "handlePrev: no current queue id")
            return@withLock
        }
        val playMode = stateDataStore.playMode.first()
        val currentOrder = queueOps.getOrder(currentQueueId, playMode)
        if (currentOrder == null) {
            Log.w(TAG, "handlePrev: order not found for queueId=$currentQueueId")
            return@withLock
        }
        val prevQueueId = queueOps.getPrevQueueId(currentOrder, playMode)
            ?: queueOps.getLastQueueId(playMode)
        if (prevQueueId == null) {
            Log.w(TAG, "handlePrev: no prev queue id available")
            return@withLock
        }
        if (playbackController.hasPrev()) {
            playbackController.seekToPrev()
        } else {
            playSongWithPreload(prevQueueId, playMode, positionMs = 0L, startPlaying = true)
        }
        val songId = queueOps.getSongIdByQueueId(prevQueueId) ?: return@withLock
        savePlaybackState(prevQueueId, songId, 0L)
        scope.launch { preloadNext(prevQueueId, playMode) }
    }

    private suspend fun handleTogglePlay() {
        when (uiState.value.playbackState) {
            PlaybackState.PLAYING -> {
                playbackController.pause()
                uiState.update { it.copy(playbackState = PlaybackState.PAUSED) }
            }
            PlaybackState.PAUSED -> {
                playbackController.play()
                uiState.update { it.copy(playbackState = PlaybackState.PLAYING) }
            }
            else -> Unit
        }
    }

    private suspend fun handleSeekTo(action: PlayAction.SeekTo) {
        playbackController.seekTo(action.positionMs)
        stateDataStore.saveCurrentPosition(action.positionMs)
        currentPositionMs.value = action.positionMs
    }

    private suspend fun handleInsertSingle(action: PlayAction.InsertSingle) {
        val currentQueueId = stateDataStore.currentQueueId.first()
        if (currentQueueId == 0L) {
            playAsNewQueue(listOf(action.songId), action.songId)
        } else {
            queueOps.insertNext(currentQueueId, listOf(action.songId))
            refreshPreload(currentQueueId)
        }
    }

    private suspend fun handleInsertGroup(action: PlayAction.InsertGroup) {
        val songIds = queueOps.resolveSongIds(action.playContext)
        if (songIds.isEmpty()) return
        val currentQueueId = stateDataStore.currentQueueId.first()
        if (currentQueueId == 0L) {
            playAsNewQueue(songIds, songIds.first())
        } else {
            queueOps.insertNext(currentQueueId, songIds)
            refreshPreload(currentQueueId)
        }
    }

    private suspend fun playAsNewQueue(songIds: List<Long>, targetSongId: Long) {
        val playMode = stateDataStore.playMode.first()
        val targetQueueId = queueOps.playNewQueue(songIds, targetSongId, playMode) ?: return
        playSongWithPreload(targetQueueId, playMode, positionMs = 0L, startPlaying = true)
        savePlaybackState(targetQueueId, targetSongId, 0L)
    }

    private suspend fun handleTogglePlayMode() {
        val currentQueueId = stateDataStore.currentQueueId.first()
        if (currentQueueId == 0L) return
        val currentMode = stateDataStore.playMode.first()
        val nextMode = currentMode.next()
        when (nextMode) {
            PlayMode.SHUFFLE -> {
                stateDataStore.saveShuffleSeed(System.currentTimeMillis())
                queueOps.switchToShuffle(currentQueueId)
            }
            PlayMode.REPEAT -> queueOps.switchToSequential()
            PlayMode.REPEAT_ONE -> queueOps.switchToRepeatOne()
        }
        stateDataStore.savePlayMode(nextMode)
        refreshPreload(currentQueueId)
        syncUiState()
    }

    private suspend fun handleRemoveFromQueue(queueId: Long) {
        val isCurrentSong = stateDataStore.currentQueueId.first() == queueId
        val nextQueueIdAfterRemove: Long?
        val prevQueueIdAfterRemove: Long?
        if (isCurrentSong) {
            val playMode = stateDataStore.playMode.first()
            val currentOrder = queueOps.getOrder(queueId, playMode)
            nextQueueIdAfterRemove = currentOrder?.let { queueOps.getNextQueueId(it, playMode) }
            prevQueueIdAfterRemove = currentOrder?.let { queueOps.getPrevQueueId(it, playMode) }
        } else {
            nextQueueIdAfterRemove = null
            prevQueueIdAfterRemove = null
        }
        queueOps.removeByQueueId(queueId)
        if (isCurrentSong) {
            val remainingSize = queueOps.getQueueSize()
            if (remainingSize == 0) {
                playbackController.pause()
                stateDataStore.saveCurrentQueueId(0L)
                stateDataStore.saveCurrentSongId(0L)
                stateDataStore.saveCurrentPosition(0L)
                uiState.value = PlayUiState.Empty
            } else {
                val targetQueueId = nextQueueIdAfterRemove ?: prevQueueIdAfterRemove
                if (targetQueueId != null) handleSeekToQueueItem(targetQueueId)
            }
        } else {
            val currentQueueId = stateDataStore.currentQueueId.first()
            if (currentQueueId != 0L) refreshPreload(currentQueueId)
        }
    }

    // ==================== 引擎事件处理 ====================

    private suspend fun handleEngineEvent(event: EngineEvent) {
        when (event) {
            is EngineEvent.OnItemTransition -> handleItemTransition(event.queueId, event.reason)
            is EngineEvent.OnPositionUpdate -> currentPositionMs.value = event.positionMs
            is EngineEvent.OnIsPlayingChanged -> {
                // 播放状态由 PlayerSession 在每个操作后主动设置，
                // 此处仅处理外部触发的状态变化（如蓝牙断开、音频焦点丢失）
                val currentState = uiState.value.playbackState
                val engineState = if (event.isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                if (currentState != engineState) {
                    uiState.update { it.copy(playbackState = engineState) }
                }
            }
        }
    }

    private suspend fun handleItemTransition(queueId: Long?, reason: Int) {
        val validQueueId = queueId ?: return
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) return
        val songId = queueOps.getSongIdByQueueId(validQueueId) ?: return
        savePlaybackState(validQueueId, songId, 0L)
        val playMode = stateDataStore.playMode.first()
        scope.launch { preloadNext(validQueueId, playMode) }
    }

    // ==================== 进程恢复 ====================

    private suspend fun restoreFromDatabase() {
        val currentQueueId = stateDataStore.currentQueueId.first()
        if (currentQueueId == 0L || queueOps.getQueueSize() == 0) return
        val playMode = stateDataStore.playMode.first()
        val shuffleSeed = stateDataStore.shuffleSeed.first()
        if (playMode == PlayMode.SHUFFLE && shuffleSeed != 0L) {
            queueOps.reassignShuffleOrdersWithSeed(currentQueueId, shuffleSeed)
        }
        val positionMs = stateDataStore.currentPositionMs.first()
        playSongWithPreload(currentQueueId, playMode, positionMs, startPlaying = false)
        currentPositionMs.update { positionMs }
        uiState.update { it.copy(playbackState = PlaybackState.PAUSED) }
        syncUiState()
    }

    // ==================== 播放核心 ====================

    private suspend fun playSongWithPreload(
        queueId: Long,
        playMode: PlayMode,
        positionMs: Long,
        startPlaying: Boolean
    ) {
        val currentSong = queueOps.getQueueSongById(queueId) ?: return
        val currentOrder = queueOps.getOrder(queueId, playMode)
        val nextSong = currentOrder
            ?.let { queueOps.getNextQueueId(it, playMode) }
            ?.let { queueOps.getQueueSongById(it) }
        val songs = listOfNotNull(currentSong, nextSong)
        playbackController.setQueue(songs, startIndex = 0, positionMs = positionMs)
        if (startPlaying) {
            playbackController.play()
            uiState.update { it.copy(playbackState = PlaybackState.PLAYING) }
        }
    }

    private suspend fun refreshPreload(currentQueueId: Long) {
        val playMode = stateDataStore.playMode.first()
        val currentOrder = queueOps.getOrder(currentQueueId, playMode)
        val nextSong = currentOrder
            ?.let { queueOps.getNextQueueId(it, playMode) }
            ?.let { queueOps.getQueueSongById(it) }
        playbackController.replacePreload(listOfNotNull(nextSong))
    }

    private suspend fun preloadNext(currentQueueId: Long, playMode: PlayMode) {
        val currentOrder = queueOps.getOrder(currentQueueId, playMode)
        val nextSong = currentOrder
            ?.let { queueOps.getNextQueueId(it, playMode) }
            ?.let { queueOps.getQueueSongById(it) }
        playbackController.replacePreload(listOfNotNull(nextSong))
    }

    // ==================== 状态辅助 ====================

    private suspend fun savePlaybackState(queueId: Long, songId: Long, position: Long) {
        stateDataStore.saveCurrentQueueId(queueId)
        stateDataStore.saveCurrentSongId(songId)
        stateDataStore.saveCurrentPosition(position)
        syncUiState()
    }

    private suspend fun syncUiState() {
        val state = stateDataStore.persistentState.first()
        uiState.update {
            it.copy(
                currentQueueId = state.currentQueueId.nullIfZero,
                currentSongId = state.currentSongId.nullIfZero,
                playMode = state.playMode
            )
        }
    }

    // ==================== 扩展 ====================

    private val Long.nullIfZero: Long? get() = if (this == 0L) null else this

    private fun PlayMode.next(): PlayMode = when (this) {
        PlayMode.REPEAT -> PlayMode.SHUFFLE
        PlayMode.SHUFFLE -> PlayMode.REPEAT_ONE
        PlayMode.REPEAT_ONE -> PlayMode.REPEAT
    }

    private companion object {
        const val TAG = "PlayerSession"
    }
}
