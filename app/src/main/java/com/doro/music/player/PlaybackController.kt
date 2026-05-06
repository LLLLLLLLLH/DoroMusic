package com.doro.music.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlaybackEvent
import com.doro.music.data.model.PlaybackState
import com.doro.music.data.model.Song
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

class PlaybackController(
    private val context: Context
) {

    private companion object {
        private const val TAG = "MusicServiceConnection"
    }

    private var mediaController: MediaController? = null
    private var future: ListenableFuture<MediaController>? = null

    private val eventChannel = Channel<PlaybackEvent>(Channel.BUFFERED)
    val eventFlow: Flow<PlaybackEvent> = eventChannel.receiveAsFlow()

    val isConnected: Boolean
        get() = mediaController != null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val state = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
            eventChannel.trySend(PlaybackEvent.OnPlaybackStateChanged(state))
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            val mode = when (repeatMode) {
                Player.REPEAT_MODE_ALL -> if (mediaController?.shuffleModeEnabled == true) {
                    PlayMode.SHUFFLE
                } else {
                    PlayMode.REPEAT
                }

                Player.REPEAT_MODE_ONE -> PlayMode.REPEAT_ONE
                else -> PlayMode.REPEAT
            }
            eventChannel.trySend(PlaybackEvent.OnPlayModeChanged(mode))
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            val currentRepeatMode = mediaController?.repeatMode ?: Player.REPEAT_MODE_ALL
            val mode = when (currentRepeatMode) {
                Player.REPEAT_MODE_ONE -> PlayMode.REPEAT_ONE
                else -> if (shuffleModeEnabled) PlayMode.SHUFFLE else PlayMode.REPEAT
            }
            eventChannel.trySend(PlaybackEvent.OnPlayModeChanged(mode))
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            eventChannel.trySend(PlaybackEvent.OnPositionChanged(newPosition.positionMs))
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.mediaMetadata?.let { metadata ->
                val song = Song(
                    id = mediaItem.mediaId.toLongOrNull() ?: 0,
                    title = metadata.title?.toString() ?: "",
                    artist = metadata.artist?.toString(),
                    album = metadata.albumTitle?.toString(),
                    duration = metadata.durationMs?.takeIf { it > 0 } ?: 0,
                    uri = mediaItem.requestMetadata.searchQuery,
                    albumArt = metadata.artworkUri?.toString(),
                    path = "",
                    mimeType = ""
                )
                eventChannel.trySend(PlaybackEvent.OnMediaItemTransition(song, reason))
            }
            mediaController?.contentDuration?.takeIf { it > 0L }?.let { duration ->
                eventChannel.trySend(PlaybackEvent.OnDurationChanged(duration))
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_READY -> {
                    mediaController?.contentDuration?.takeIf { it > 0L }?.let { duration ->
                        eventChannel.trySend(PlaybackEvent.OnDurationChanged(duration))
                    }
                    if (mediaController?.isPlaying == true) PlaybackState.PLAYING else PlaybackState.PAUSED
                }

                Player.STATE_BUFFERING -> PlaybackState.PAUSED
                Player.STATE_ENDED -> PlaybackState.PAUSED
                Player.STATE_IDLE -> PlaybackState.IDLE
                else -> PlaybackState.IDLE
            }
            eventChannel.trySend(PlaybackEvent.OnPlaybackStateChanged(state))
        }

        override fun onPlayerError(error: PlaybackException) {
            eventChannel.trySend(PlaybackEvent.OnError(Exception(error.message, error.cause)))
        }
    }

    fun connect() {
        if (isConnected) return
        try {
            val token = SessionToken(context, ComponentName(context, MusicService::class.java))
            future = MediaController.Builder(context, token).buildAsync()

            future?.addListener({
                try {
                    val controller = future?.get()
                    if (controller != null) {
                        controller.addListener(playerListener)
                        mediaController = controller
                    } else {
                        Log.e(TAG, "MediaController is null after build")
                        mediaController = null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get MediaController", e)
                    mediaController = null
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start build", e)
            mediaController = null
        }
    }


    suspend fun disconnect() {
        withContext(Dispatchers.Main) {
            mediaController?.removeListener(playerListener)
            mediaController?.release()
            mediaController = null
            future?.cancel(true)
            future = null
        }
    }

    suspend fun play() = withContext(Dispatchers.Main) {
        mediaController?.play()
    }

    suspend fun pause() = withContext(Dispatchers.Main) {
        mediaController?.pause()
    }

    suspend fun loadPlaylist(songs: List<Song>, startIndex: Int) = withContext(Dispatchers.Main) {
        mediaController?.let {
            it.setMediaItems(songs.map { song -> song.toMediaItem() }, startIndex, 0L)
            it.prepare()
        }
    }

    suspend fun addToQueue(songs: List<Song>, insertIndex: Int) = withContext(Dispatchers.Main) {
        mediaController?.let {
            val insertPos = (insertIndex + 1).coerceAtMost(it.mediaItemCount)
            it.addMediaItems(insertPos, songs.map { song -> song.toMediaItem() })
        }
    }

    suspend fun removeFromQueue(index: Int) = withContext(Dispatchers.Main) {
        mediaController?.let { if (index in 0 until it.mediaItemCount) it.removeMediaItem(index) }
    }

    suspend fun seekToQueueItem(index: Int) = withContext(Dispatchers.Main) {
        mediaController?.let { if (index in 0 until it.mediaItemCount) it.seekToDefaultPosition(index) }
    }

    suspend fun seekTo(positionMs: Long) = withContext(Dispatchers.Main) { mediaController?.seekTo(positionMs.coerceAtLeast(0L)) }

    suspend fun getCurrentPosition() = withContext(Dispatchers.Main) { mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L }

    suspend fun setPlayMode(mode: PlayMode) = withContext(Dispatchers.Main) {
        mediaController?.let {
            it.repeatMode = when (mode) {
                PlayMode.SHUFFLE, PlayMode.REPEAT -> Player.REPEAT_MODE_ALL
                PlayMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
            }
            it.shuffleModeEnabled = mode == PlayMode.SHUFFLE
        }
    }

    data class ControllerState(
        val songs: List<Song>,
        val currentIndex: Int,
        val currentPosition: Long,
        val duration: Long,
        val isPlaying: Boolean
    )

    suspend fun getCurrentState(): ControllerState? = withContext(Dispatchers.Main) {
        val controller = mediaController ?: return@withContext null
        val songs = (0 until controller.mediaItemCount).mapNotNull { index ->
            controller.getMediaItemAt(index).let { item ->
                item.mediaMetadata.let { metadata ->
                    Song(
                        id = item.mediaId.toLongOrNull() ?: 0,
                        title = metadata.title?.toString() ?: "",
                        artist = metadata.artist?.toString(),
                        album = metadata.albumTitle?.toString(),
                        duration = metadata.durationMs?.takeIf { it > 0 } ?: 0,
                        uri = item.requestMetadata.searchQuery,
                        albumArt = metadata.artworkUri?.toString(),
                        path = "",
                        mimeType = ""
                    )
                }
            }
        }
        if (songs.isEmpty()) return@withContext null
        ControllerState(
            songs = songs,
            currentIndex = controller.currentMediaItemIndex.coerceIn(0, songs.lastIndex),
            currentPosition = controller.currentPosition.coerceAtLeast(0L),
            duration = controller.contentDuration.coerceAtLeast(0L),
            isPlaying = controller.isPlaying
        )
    }
}
