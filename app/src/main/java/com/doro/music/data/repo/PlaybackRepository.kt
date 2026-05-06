package com.doro.music.data.repo

import android.util.Log
import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlaybackEvent
import com.doro.music.data.model.PlaybackSnapshot
import com.doro.music.data.model.PlaybackState
import com.doro.music.data.model.Song
import com.doro.music.player.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.math.min

class PlaybackRepository(
    private val musicServiceConnection: PlaybackController,
    private val playbackStateSaver: PlaybackStateSaver,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())
) {

    private companion object {
        private const val TAG = "PlaybackRepository"
        private const val POSITION_UPDATE_INTERVAL_MS = 200L
    }

    private val snapshot = MutableStateFlow(PlaybackSnapshot.EMPTY)
    private val commandMutex = Mutex()
    private var previousSnapshot = PlaybackSnapshot.EMPTY

    val playbackState: StateFlow<PlaybackState>
        field = MutableStateFlow(PlaybackState.IDLE)

    val currentSong: StateFlow<Song?>
        field = MutableStateFlow<Song?>(null)

    val playQueue: StateFlow<List<Song>>
        field = MutableStateFlow<List<Song>>(emptyList())

    val currentIndex: StateFlow<Int>
        field = MutableStateFlow(0)

    val currentPosition: StateFlow<Long>
        field = MutableStateFlow(0L)

    val duration: StateFlow<Long>
        field = MutableStateFlow(0L)

    val playMode: StateFlow<PlayMode>
        field = MutableStateFlow(PlayMode.REPEAT)

    val isLoading: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val errorMessage: StateFlow<String?>
        field = MutableStateFlow<String?>(null)

    val canNext: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val canPrevious: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private var positionUpdateJob: Job? = null
    private var eventListenerJob: Job? = null

    fun initialize() {
        try {
            musicServiceConnection.connect()
            listenToServiceEvents()
            startPositionUpdates()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize", e)
            errorMessage.value = e.message
        }
    }

    suspend fun release() {
        stopAllJobs()
        musicServiceConnection.disconnect()
    }

    suspend fun togglePlayPause() {
        if (snapshot.value.isPlaying) pause() else play()
    }

    suspend fun playSongs(songs: List<Song>, startIndex: Int) = executeCommand {
        updateSnapshot {
            it.copy(songs = songs, currentIndex = startIndex, currentPosition = 0, duration = songs[startIndex].duration, isPlaying = true)
        }
        musicServiceConnection.loadPlaylist(songs, startIndex)
        musicServiceConnection.play()
    }

    suspend fun addToQueue(songs: List<Song>) = executeCommand {
        val current = snapshot.value
        val newSnapshot = if (current.songs.isEmpty()) {
            current.copy(songs = songs, currentIndex = 0, isPlaying = true)
        } else {
            val insertPos = (current.currentIndex + 1).coerceAtMost(current.songs.size)
            current.copy(songs = current.songs.toMutableList().apply { addAll(insertPos, songs) })
        }
        snapshot.value = newSnapshot
        updateStates(newSnapshot)
        musicServiceConnection.addToQueue(songs, snapshot.value.currentIndex)
    }

    suspend fun removeFromQueue(index: Int) = executeCommand {
        val current = snapshot.value
        val newSongs = current.songs.toMutableList().also { it.removeAt(index) }

        if (newSongs.isEmpty()) {
            playbackState.value = PlaybackState.IDLE
            updateSnapshot { PlaybackSnapshot.EMPTY }
            musicServiceConnection.removeFromQueue(index)
            return@executeCommand
        }

        val newIndex = when {
            index < current.currentIndex -> current.currentIndex - 1
            index == current.currentIndex -> min(current.currentIndex, newSongs.lastIndex)
            else -> current.currentIndex
        }

        updateSnapshot { it.copy(songs = newSongs, currentIndex = newIndex) }

        musicServiceConnection.removeFromQueue(index)
    }


    suspend fun seekToQueueItem(index: Int) = executeCommand {
        updateSnapshot { it.copy(currentIndex = index, currentPosition = 0) }
        musicServiceConnection.seekToQueueItem(index)
        musicServiceConnection.play()
    }

    suspend fun seekTo(positionMs: Long) = executeCommand {
        updateSnapshot { it.copy(currentPosition = positionMs) }
        musicServiceConnection.seekTo(positionMs)
    }

    suspend fun next() {
        val snap = snapshot.value
        if (!snap.canNext()) return
        seekToQueueItem((snap.currentIndex + 1) % snap.songs.size)
    }

    suspend fun previous() {
        val snap = snapshot.value
        if (!snap.canPrevious()) return
        val prevIndex = if (snap.currentIndex == 0) snap.songs.lastIndex else snap.currentIndex - 1
        seekToQueueItem(prevIndex)
    }

    suspend fun setPlayMode(mode: PlayMode) = executeCommand {
        updateSnapshot { it.copy(playMode = mode) }
        musicServiceConnection.setPlayMode(mode)
    }

    suspend fun togglePlayMode() {
        val nextMode = when (snapshot.value.playMode) {
            PlayMode.REPEAT -> PlayMode.SHUFFLE
            PlayMode.SHUFFLE -> PlayMode.REPEAT_ONE
            PlayMode.REPEAT_ONE -> PlayMode.REPEAT
        }
        setPlayMode(nextMode)
    }

    private suspend fun play() = executeCommand {
        updateSnapshot { it.copy(isPlaying = true) }
        musicServiceConnection.play()
    }

    private suspend fun pause() = executeCommand {
        updateSnapshot { it.copy(isPlaying = false) }
        musicServiceConnection.pause()
    }

    private suspend inline fun executeCommand(crossinline block: suspend () -> Unit) {
        if (!commandMutex.tryLock()) return
        previousSnapshot = snapshot.value
        try {
            block()
            errorMessage.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Command error", e)
            errorMessage.value = e.message
            snapshot.value = previousSnapshot
            updateStates(previousSnapshot)
        } finally {
            commandMutex.unlock()
        }
    }

    private fun updateSnapshot(transform: (PlaybackSnapshot) -> PlaybackSnapshot) {
        val newSnapshot = transform(snapshot.value)
        snapshot.value = newSnapshot
        updateStates(newSnapshot)
    }

    private fun syncStateFromController() {
        scope.launch {
            val state = musicServiceConnection.getCurrentState() ?: return@launch
            val restored = PlaybackSnapshot(
                songs = state.songs,
                currentIndex = state.currentIndex,
                currentPosition = state.currentPosition,
                duration = state.duration,
                isPlaying = state.isPlaying
            )
            snapshot.value = restored
            updateStates(restored)
        }
    }

    private fun listenToServiceEvents() {
        eventListenerJob = scope.launch {
            musicServiceConnection.eventFlow.collect { event -> handlePlaybackEvent(event) }
        }
    }

    private fun handlePlaybackEvent(event: PlaybackEvent) {
        try {
            when (event) {
                is PlaybackEvent.OnPlaybackStateChanged -> {
                    if (event.state != PlaybackState.IDLE && snapshot.value.songs.isEmpty()) {
                        syncStateFromController()
                    }
                    snapshot.value = snapshot.value.copy(isPlaying = event.state == PlaybackState.PLAYING)
                    playbackState.value = event.state
                }

                is PlaybackEvent.OnPositionChanged -> {
                    snapshot.value = snapshot.value.copy(currentPosition = event.positionMs)
                    currentPosition.value = event.positionMs
                }

                is PlaybackEvent.OnDurationChanged -> {
                    snapshot.value = snapshot.value.copy(duration = event.durationMs)
                    duration.value = event.durationMs
                }

                is PlaybackEvent.OnMediaItemTransition -> {
                    if (!commandMutex.isLocked) {
                        event.song?.let { song ->
                            val index = snapshot.value.songs.indexOfFirst { it.id == song.id }
                            if (index >= 0) {
                                snapshot.value = snapshot.value.copy(currentIndex = index)
                                currentIndex.value = index
                            }
                        }
                    }
                }

                is PlaybackEvent.OnPlayModeChanged -> {
                    snapshot.value = snapshot.value.copy(playMode = event.mode)
                    playMode.value = event.mode
                }

                is PlaybackEvent.OnError -> {
                    playbackState.value = PlaybackState.ERROR
                    errorMessage.value = event.exception.message
                }

                is PlaybackEvent.OnPlaylistRestored -> Unit
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling event", e)
        }
    }

    private fun updateStates(snap: PlaybackSnapshot) {
        playQueue.value = snap.songs
        currentSong.value = snap.getCurrentSong()
        currentIndex.value = snap.currentIndex
        currentPosition.value = snap.currentPosition
        duration.value = snap.duration
        playMode.value = snap.playMode
        canNext.value = snap.canNext()
        canPrevious.value = snap.canPrevious()
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                if (snapshot.value.songs.isNotEmpty() && musicServiceConnection.isConnected) {
                    try {
                        val pos = musicServiceConnection.getCurrentPosition()
                        snapshot.value = snapshot.value.copy(currentPosition = pos)
                        currentPosition.value = pos
                    } catch (_: Exception) {
                    }
                }
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopAllJobs() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        eventListenerJob?.cancel()
        eventListenerJob = null
    }
}
