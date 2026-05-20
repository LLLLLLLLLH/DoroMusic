package com.doro.music.player.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.doro.music.R
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.model.PlayAction
import com.doro.music.ui.MainActivity
import org.koin.android.ext.android.inject

@OptIn(UnstableApi::class)
class PlayerService : MediaSessionService() {

    private val dispatcher: PlayActionDispatcher by inject()

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val exoPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, createForwardingPlayer(exoPlayer))
            .build()

        createSessionActivity()?.let { mediaSession?.setSessionActivity(it) }

        setMediaNotificationProvider(createNotificationProvider())
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun createSessionActivity(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntentCompat.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            false
        )
    }

    private fun createNotificationProvider(): DefaultMediaNotificationProvider {
        return DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.notification_channel_playback)
            .setNotificationId(NOTIFICATION_ID)
            .build()
            .also { it.setSmallIcon(R.drawable.ic_notification) }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(getString(R.string.notification_channel_playback))
            .setSound(null, null)
            .setShowBadge(false)
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    @UnstableApi
    private fun createForwardingPlayer(player: Player) = object : ForwardingPlayer(player) {
        override fun seekToNext() {
            dispatcher.dispatch(PlayAction.Next)
        }

        override fun seekToPrevious() {
            dispatcher.dispatch(PlayAction.Prev)
        }

        override fun getAvailableCommands(): Player.Commands {
            return super.getAvailableCommands().buildUpon()
                .addAll(*SUPPORTED_COMMANDS)
                .build()
        }

        override fun isCommandAvailable(command: Int): Boolean {
            return command in SUPPORTED_COMMANDS || super.isCommandAvailable(command)
        }
    }

    companion object {
        private const val CHANNEL_ID = "doro_music"
        private const val NOTIFICATION_ID = 1001

        private val SUPPORTED_COMMANDS = intArrayOf(
            COMMAND_SEEK_TO_NEXT,
            COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            COMMAND_SEEK_TO_PREVIOUS,
            COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
        )
    }
}
