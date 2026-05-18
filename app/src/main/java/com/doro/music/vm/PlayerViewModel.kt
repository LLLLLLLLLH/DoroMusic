@file:OptIn(ExperimentalCoroutinesApi::class)

package com.doro.music.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.Song
import com.doro.music.data.repo.SongRepo
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.PlayStateObserver
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayUiState
import com.doro.music.ui.component.player.PlayerSheetState
import com.doro.music.ui.component.player.PlayerViewType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 播放器 ViewModel
 *
 * 连接播放架构与 UI 层：
 * - 从 PlayStateObserver 读取播放状态和进度
 * - 通过 PlayActionDispatcher 分发用户操作
 * - 通过 PlayStateObserver.playQueue 提供播放队列 Paging3 数据
 */
class PlayerViewModel(
    private val stateObserver: PlayStateObserver,
    private val actionDispatcher: PlayActionDispatcher,
    private val songRepo: SongRepo
) : ViewModel() {

    private companion object {
        const val TAG = "PlayerViewModel"
    }

    // ==================== 核心状态 ====================

    val uiState: StateFlow<PlayUiState> = stateObserver.uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PlayUiState.Empty
    )

    /** 当前播放位置（高频数据，独立 Flow） */
    val currentPosition: StateFlow<Long> = stateObserver.currentPositionMs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0L
    )

    val currentSong: StateFlow<Song?> = uiState
        .mapLatest { state -> state.currentSongId?.let { songRepo.getSongById(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // ==================== 播放队列 Paging3 ====================

    val playQueuePaging = stateObserver.playQueue.cachedIn(viewModelScope)

    // ==================== UI 状态 ====================

    val playerViewType: MutableStateFlow<PlayerViewType> = MutableStateFlow(PlayerViewType.DISC)
    val playerSheetState: MutableStateFlow<PlayerSheetState> = MutableStateFlow(PlayerSheetState.Hidden)
    val isQueueVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        // 监听播放状态，自动控制 playerSheetState
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.currentQueueId == null) {
                    playerSheetState.value = PlayerSheetState.Hidden
                    isQueueVisible.value = false
                } else if (playerSheetState.value == PlayerSheetState.Hidden) {
                    playerSheetState.value = PlayerSheetState.Collapsed
                }
            }
        }
    }

    // ==================== 播放操作 ====================

    /**
     * 处理 UI 层的 PlayerAction
     */
    fun handlePlayerAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.TogglePlayPause -> actionDispatcher.dispatch(PlayAction.TogglePlay)
            is PlayerAction.Next -> actionDispatcher.dispatch(PlayAction.Next)
            is PlayerAction.Previous -> actionDispatcher.dispatch(PlayAction.Prev)
            is PlayerAction.SeekTo -> actionDispatcher.dispatch(PlayAction.SeekTo(action.positionMs))
            is PlayerAction.TogglePlayMode -> actionDispatcher.dispatch(PlayAction.TogglePlayMode)
            is PlayerAction.TogglePlayerView -> togglePlayerView()
            is PlayerAction.TogglePlayerSheet -> togglePlayerSheet()
            is PlayerAction.TogglePlayQueue -> togglePlayQueue()
        }
    }

    /**
     * 添加单首歌曲到下一首播放
     */
    fun addToNext(song: Song) {
        actionDispatcher.dispatch(PlayAction.InsertSingle(song.id))
    }

    /**
     * 从播放队列移除指定 queueId 的歌曲
     */
    fun removeFromPlayQueue(queueId: Long) {
        actionDispatcher.dispatch(PlayAction.Remove(queueId))
    }

    fun seekToQueueItem(queueId: Long) {
        actionDispatcher.dispatch(PlayAction.SeekToQueueItem(queueId))
    }

    // ==================== UI 辅助方法 ====================

    private fun togglePlayerView() {
        playerViewType.value = when (playerViewType.value) {
            PlayerViewType.DISC -> PlayerViewType.LYRIC
            PlayerViewType.LYRIC -> PlayerViewType.DISC
        }
    }

    private fun togglePlayerSheet() {
        if (playerSheetState.value == PlayerSheetState.Hidden) return
        playerSheetState.value = when (playerSheetState.value) {
            PlayerSheetState.Expanded -> PlayerSheetState.Collapsed
            else -> PlayerSheetState.Expanded
        }
    }

    private fun togglePlayQueue() {
        isQueueVisible.value = !isQueueVisible.value
    }

    fun handleBack(): Boolean {
        return when {
            isQueueVisible.value -> {
                isQueueVisible.value = false; true
            }

            playerSheetState.value == PlayerSheetState.Expanded -> {
                playerSheetState.value = PlayerSheetState.Collapsed; true
            }

            else -> false
        }
    }
}