package com.doro.music.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.doro.music.R
import com.doro.music.data.datastore.PlayStateDataStore
import com.doro.music.data.repo.SongRepo
import com.doro.music.ui.MainActivity
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import androidx.core.net.toUri

/** Widget 动作参数 Key */
val ACTION_KEY = ActionParameters.Key<String>("player_action")

/** 动作常量 */
const val ACTION_TOGGLE_PLAY = "toggle_play"
const val ACTION_NEXT = "next"
const val ACTION_PREV = "prev"

class MusicPlayerWidget : GlanceAppWidget(), KoinComponent {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 80.dp),
            DpSize(250.dp, 110.dp),
            DpSize(320.dp, 140.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val playStateDataStore: PlayStateDataStore by inject()
        val songRepo: SongRepo by inject()

        val songId = playStateDataStore.currentSongId.first()
        val song = if (songId != 0L) songRepo.getSongById(songId) else null

        provideContent {
            GlanceTheme {
                WidgetContent(
                    title = song?.title ?: context.getString(R.string.widget_no_song),
                    artist = song?.artist ?: context.getString(R.string.widget_no_artist),
                    albumArtUri = song?.albumArt,
                    isPlaying = false,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    title: String,
    artist: String,
    albumArtUri: String?,
    isPlaying: Boolean,
    context: Context
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArt(albumArtUri = albumArtUri, context = context)

            Spacer(modifier = GlanceModifier.width(12.dp))

            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SongInfo(title = title, artist = artist)
                Spacer(modifier = GlanceModifier.height(8.dp))
                PlaybackControls(isPlaying = isPlaying, context = context)
            }
        }
    }
}

@Composable
private fun AlbumArt(albumArtUri: String?, context: Context) {
    val bitmap = albumArtUri?.let { loadAlbumArt(it, context) }
    val imageProvider = if (bitmap != null) {
        ImageProvider(bitmap)
    } else {
        ImageProvider(R.drawable.ic_widget_placeholder)
    }

    Image(
        provider = imageProvider,
        contentDescription = context.getString(R.string.album_cover),
        modifier = GlanceModifier
            .size(64.dp)
            .cornerRadius(8.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun SongInfo(title: String, artist: String) {
    Column {
        Text(
            text = title,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            maxLines = 1
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = artist,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 13.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun PlaybackControls(isPlaying: Boolean, context: Context) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一首
        Image(
            provider = ImageProvider(R.drawable.ic_skip_previous),
            contentDescription = context.getString(R.string.previous_track),
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(actionRunCallback<PlayerActionCallback>(
                    parameters = actionParametersOf(ACTION_KEY to ACTION_PREV)
                ))
        )

        Spacer(modifier = GlanceModifier.width(16.dp))

        // 播放/暂停
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        Image(
            provider = ImageProvider(playPauseIcon),
            contentDescription = context.getString(R.string.play),
            modifier = GlanceModifier
                .size(48.dp)
                .clickable(actionRunCallback<PlayerActionCallback>(
                    parameters = actionParametersOf(ACTION_KEY to ACTION_TOGGLE_PLAY)
                ))
        )

        Spacer(modifier = GlanceModifier.width(16.dp))

        // 下一首
        Image(
            provider = ImageProvider(R.drawable.ic_skip_next),
            contentDescription = context.getString(R.string.next_track),
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(actionRunCallback<PlayerActionCallback>(
                    parameters = actionParametersOf(ACTION_KEY to ACTION_NEXT)
                ))
        )
    }
}

private fun loadAlbumArt(uri: String, context: Context): Bitmap? {
    return try {
        val contentUri = uri.toUri()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(
                context.contentResolver,
                contentUri
            )
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                contentUri
            )
        }
    } catch (_: Exception) {
        null
    }
}
