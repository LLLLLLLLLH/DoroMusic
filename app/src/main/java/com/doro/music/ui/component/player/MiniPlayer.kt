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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.doro.music.R
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
    val playbackIcon = remember(playbackState) {
        if (playbackState == PlaybackState.PLAYING) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
    }

    Row(
        modifier = modifier
            .graphicsLayer { alpha = 1F - sheetProgressState.progress }
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onActionClick(PlayerAction.TogglePlayerSheet) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SongInfoContent(
            title = song?.title,
            artist = song?.artist,
            albumArt = song?.albumArt,
            modifier = Modifier.weight(1f) // 权重在这里传递
        )

        PlayerControls(
            playbackIcon = playbackIcon,
            playbackState = playbackState,
            onActionClick = onActionClick
        )
    }
}

@Composable
private fun SongInfoContent(
    title: String?,
    artist: String?,
    albumArt: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkImage(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp)),
            imageUrl = albumArt
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artist.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayerControls(
    playbackIcon: ImageVector,
    playbackState: PlaybackState,
    onActionClick: (PlayerAction) -> Unit
) {
    val playPauseDescription = stringResource(
        if (playbackState == PlaybackState.PLAYING) R.string.pause else R.string.play
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onActionClick(PlayerAction.TogglePlayPause) }) {
            Icon(
                imageVector = playbackIcon,
                contentDescription = playPauseDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { onActionClick(PlayerAction.Next) }) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = stringResource(R.string.next_track),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
