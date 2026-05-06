package com.doro.music.ui.component.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doro.music.data.model.PlaybackState
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.Song
import com.doro.music.ui.component.ArtworkImage
import com.doro.music.ui.component.SheetProgressState

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    sheetProgressState: SheetProgressState,
    song: Song?,
    playbackState: PlaybackState,
    onActionClick: (PlayerAction) -> Unit = {}
) {
    val playbackIcon = if (playbackState == PlaybackState.PLAYING) Icons.Rounded.Pause else Icons.Rounded.PlayArrow

    Row(
        modifier = modifier
            .graphicsLayer { this.alpha = 1F - sheetProgressState.progress }
            .clickable(
                onClick = { onActionClick(PlayerAction.TogglePlayerSheet) }
            )
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ArtworkImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                imageUrl = song?.albumArt
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = song?.title.orEmpty(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song?.artist.orEmpty(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row {
            IconButton(onClick = { onActionClick(PlayerAction.TogglePlayPause) }) {
                Icon(
                    imageVector = playbackIcon,
                    contentDescription = playbackIcon.name,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = { onActionClick(PlayerAction.Next) }) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = Icons.Rounded.SkipNext.name,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
