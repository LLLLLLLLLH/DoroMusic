package com.doro.music.ui.component.player

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.PlaybackState
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.Song
import com.doro.music.ext.noRippleClick
import com.doro.music.ui.component.placeholderPainter

@Composable
fun PlayerDisplay(
    modifier: Modifier = Modifier,
    song: Song? = null,
    playbackState: PlaybackState,
    currentView: PlayerViewType,
    lyrics: LyricsData? = null,
    lyricIndex: Int = -1,
    onActionClick: (PlayerAction) -> Unit
) {
    val isLandscape = rememberIsLandscape()
    val contentFillFraction = if (isLandscape) 1F else 0.8F

    Box(
        modifier = modifier.noRippleClick { onActionClick(PlayerAction.TogglePlayerView) },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentView,
            modifier = Modifier.fillMaxSize(contentFillFraction),
            contentAlignment = Alignment.Center
        ) { type ->
            when (type) {
                PlayerViewType.DISC ->
                    VinylDisc(
                        playbackState = playbackState,
                        key = { "${song?.id}${SystemClock.elapsedRealtime()}"  }
                    ) {
                        AsyncImage(
                            model = song?.albumArt,
                            contentDescription = null,
                            error = placeholderPainter(),
                            fallback = placeholderPainter(),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                PlayerViewType.LYRIC -> LyricsView(
                    lyrics = lyrics,
                    currentIndex = lyricIndex,
                    onSeekToLine = { timeMs ->
                        onActionClick(PlayerAction.SeekTo(timeMs))
                    }
                )
            }
        }
    }
}
