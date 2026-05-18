package com.doro.music.player.controller

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.doro.music.player.PlayerAccessor
import com.doro.music.player.model.QueueSong
import com.doro.music.player.model.toMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PlaybackController {
    suspend fun setQueue(songs: List<QueueSong>, startIndex: Int, positionMs: Long = 0L)
    suspend fun hasNext(): Boolean
    suspend fun hasPrev(): Boolean
    suspend fun seekToNext()
    suspend fun seekToPrev()
    suspend fun replacePreload(songs: List<QueueSong>)
    suspend fun play()
    suspend fun pause()
    suspend fun seekTo(positionMs: Long)
    val events: Flow<EngineEvent>
}

sealed class EngineEvent {

    /** 媒体项切换，queueId 为新播放项的 queue_id，reason 为切换原因 */
    data class OnItemTransition(val queueId: Long?, val reason: Int) : EngineEvent()

    /** 播放位置更新 */
    data class OnPositionUpdate(val positionMs: Long) : EngineEvent()

    /** 实际播放状态变化（isPlaying = playWhenReady && state == READY） */
    data class OnIsPlayingChanged(val isPlaying: Boolean) : EngineEvent()
}

class MediaPlaybackController(
    private val accessor: PlayerAccessor
) : PlaybackController {

    companion object {
        private const val POSITION_POLL_INTERVAL_MS = 500L
    }

    override suspend fun setQueue(songs: List<QueueSong>, startIndex: Int, positionMs: Long) = execute { controller ->
        val mediaItems = songs.map { it.toMediaItem() }
        if (mediaItems.isEmpty()) return@execute
        controller.setMediaItems(mediaItems, startIndex, positionMs)
    }

    override suspend fun hasNext(): Boolean = query { controller ->
        controller.hasNextMediaItem()
    } ?: false

    override suspend fun hasPrev(): Boolean = query { controller ->
        controller.hasPreviousMediaItem()
    } ?: false

    override suspend fun seekToNext() = execute { controller ->
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    override suspend fun seekToPrev() = execute { controller ->
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        }
    }

    override suspend fun replacePreload(songs: List<QueueSong>) = execute { controller ->
        val currentIndex = controller.currentMediaItemIndex
        while (controller.mediaItemCount > currentIndex + 1) {
            controller.removeMediaItem(controller.mediaItemCount - 1)
        }
        songs.forEach { song ->
            controller.addMediaItem(song.toMediaItem())
        }
    }

    override suspend fun play() = execute { controller ->
        controller.play()
    }

    override suspend fun pause() = execute { controller ->
        controller.pause()
    }

    override suspend fun seekTo(positionMs: Long) = execute { controller ->
        controller.seekTo(positionMs)
    }

    override val events: Flow<EngineEvent> = callbackFlow {
        val controller = accessor.controller ?: return@callbackFlow
        var positionPollingJob: Job? = null

        fun startPositionPolling() {
            if (positionPollingJob?.isActive == true) return
            positionPollingJob = launch(Dispatchers.Main) {
                while (true) {
                    trySend(EngineEvent.OnPositionUpdate(controller.currentPosition))
                    delay(POSITION_POLL_INTERVAL_MS)
                }
            }
        }

        fun stopPositionPolling() {
            positionPollingJob?.cancel()
            positionPollingJob = null
        }

        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val queueId = mediaItem?.mediaId?.toLongOrNull()
                trySend(EngineEvent.OnItemTransition(queueId, reason))
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                trySend(EngineEvent.OnIsPlayingChanged(isPlaying))
                if (isPlaying) {
                    startPositionPolling()
                } else {
                    stopPositionPolling()
                    trySend(EngineEvent.OnPositionUpdate(controller.currentPosition))
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                trySend(EngineEvent.OnPositionUpdate(controller.currentPosition))
            }
        }

        withContext(Dispatchers.Main) {
            controller.addListener(listener)
            if (controller.isPlaying) {
                startPositionPolling()
            }
        }

        awaitClose {
            stopPositionPolling()
            launch(Dispatchers.Main) { controller.removeListener(listener) }
        }
    }

    private suspend inline fun execute(noinline block: suspend (MediaController) -> Unit) =
        withContext(Dispatchers.Main) {
            val controller = accessor.controller ?: return@withContext
            block.invoke(controller)
        }

    private suspend inline fun <T> query(noinline block: suspend (MediaController) -> T): T? =
        withContext(Dispatchers.Main) {
            val controller = accessor.controller ?: return@withContext null
            block.invoke(controller)
        }
}
