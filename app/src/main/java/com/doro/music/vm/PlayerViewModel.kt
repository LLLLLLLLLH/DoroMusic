package com.doro.music.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.Song
import com.doro.music.data.repo.PlaybackRepository
import com.doro.music.ui.component.player.PlayerSheetState
import com.doro.music.ui.component.player.PlayerViewType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private companion object {
        private const val TAG = "PlayerViewModel"
    }

    val playbackState = playbackRepository.playbackState

    val currentSong = playbackRepository.currentSong

    val playQueue = playbackRepository.playQueue

    val currentIndex = playbackRepository.currentIndex

    val currentPosition = playbackRepository.currentPosition

    val duration = playbackRepository.duration

    val playMode = playbackRepository.playMode

    val playerViewType: StateFlow<PlayerViewType>
        field = MutableStateFlow(PlayerViewType.DISC)

    val playerSheetState: StateFlow<PlayerSheetState>
        field = MutableStateFlow<PlayerSheetState>(PlayerSheetState.Hidden)

    val isQueueVisible: StateFlow<Boolean>
        field = MutableStateFlow(false)

    init {
        initializeRepository()

        playQueue.onEach { queue ->
            when {
                queue.isEmpty() -> {
                    playerSheetState.value = PlayerSheetState.Hidden
                    isQueueVisible.value = false
                }
                playerSheetState.value == PlayerSheetState.Hidden -> {
                    playerSheetState.value = PlayerSheetState.Collapsed
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun initializeRepository() {
        viewModelScope.launch {
            try {
                playbackRepository.initialize()
                Log.d(TAG, "Repository initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize repository", e)
            }
        }
    }

    fun handlePlayerAction(action: PlayerAction) {
        viewModelScope.launch {
            try {
                when (action) {
                    PlayerAction.TogglePlayPause -> playbackRepository.togglePlayPause()
                    PlayerAction.Next -> playbackRepository.next()
                    PlayerAction.Previous -> playbackRepository.previous()
                    is PlayerAction.SeekTo -> playbackRepository.seekTo(action.positionMs)
                    PlayerAction.TogglePlayMode -> playbackRepository.togglePlayMode()
                    PlayerAction.TogglePlayerView -> togglePlayerView()
                    PlayerAction.TogglePlayerSheet -> togglePlayerSheet()
                    PlayerAction.TogglePlayQueue -> togglePlayQueue()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling player action: ${action::class.simpleName}", e)
            }
        }
    }

    fun addToNext(song: Song) {
        viewModelScope.launch {
            try {
                playbackRepository.addToQueue(listOf(song))
            } catch (e: Exception) {
                Log.e(TAG, "Error adding song to queue", e)
            }
        }
    }

    fun addToNext(songs: List<Song>) {
        viewModelScope.launch {
            try {
                playbackRepository.addToQueue(songs)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding songs to queue", e)
            }
        }
    }

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

    fun removeFromPlayQueue(index: Int) {
        viewModelScope.launch {
            try {
                playbackRepository.removeFromQueue(index)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing song from queue", e)
            }
        }
    }

    fun handleBack(): Boolean {
        return when {
            isQueueVisible.value -> { isQueueVisible.value = false; true }
            playerSheetState.value == PlayerSheetState.Expanded -> {
                playerSheetState.value = PlayerSheetState.Collapsed; true
            }
            else -> false
        }
    }

    fun seekToQueueItem(index: Int) {
        viewModelScope.launch {
            try {
                playbackRepository.seekToQueueItem(index)
            } catch (e: Exception) {
                Log.e(TAG, "Error seeking to queue item", e)
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch { playbackRepository.release() }
    }
}
