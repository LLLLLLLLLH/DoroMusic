package com.doro.music.player

import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.doro.music.data.repo.PlaybackStateSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val playbackStateSaver: PlaybackStateSaver by inject()

    private val player: ExoPlayer?
        get() = mediaSession?.player as? ExoPlayer

    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())

        restoreFromDB(exoPlayer)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    @UnstableApi
    override fun onTaskRemoved(rootIntent: Intent?) {
        savePlaybackStateAsync()
        pauseAllPlayersAndStopSelf()
    }

    override fun onTrimMemory(level: Int) {
        savePlaybackStateAsync()
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        savePlaybackStateAsync()
        mediaSession?.run { player.release(); release() }
        mediaSession = null
        scope.cancel()
        super.onDestroy()
    }

    private fun restoreFromDB(exoPlayer: ExoPlayer) {
        scope.launch {
            val state = playbackStateSaver.restore() ?: return@launch
            exoPlayer.setMediaItems(
                state.songs.map { it.toMediaItem() },
                state.currentIndex,
                state.currentPosition
            )
            exoPlayer.prepare()
        }
    }

    private fun savePlaybackStateAsync() {
        val p = player ?: return
        val position = p.currentPosition.coerceAtLeast(0L)
        val currentIndex = p.currentMediaItemIndex.coerceAtLeast(0)
        val songIds = (0 until p.mediaItemCount).mapNotNull { index ->
            p.getMediaItemAt(index).mediaMetadata.extras?.getLong(MediaExtras.SONG_ID)
        }
        if (songIds.isEmpty()) return

        scope.launch {
            playbackStateSaver.saveByIds(songIds, currentIndex, position)
        }
    }
}
