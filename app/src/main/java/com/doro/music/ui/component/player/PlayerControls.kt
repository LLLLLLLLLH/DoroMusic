package com.doro.music.ui.component.player

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ConstraintSetScope
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.PlaybackState
import com.doro.music.data.model.Song
import com.doro.music.ui.component.IconAction

private fun ConstraintSetScope.createPlayerControlRefIds() =
    createRefsFor(
        "artist", "title", "slider",
        "playModeButton", "prevButton", "playPauseButton",
        "nextButton", "playlistButton"
    )

private val PortraitConstraints = ConstraintSet {
    val (artist, title, slider, playModeButton, prevButton, playPauseButton, nextButton, playlistButton) = createPlayerControlRefIds()

    val guideBottom = createGuidelineFromBottom(0.1F)

    constrain(artist) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
    constrain(title) {
        top.linkTo(artist.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
    constrain(slider) {
        top.linkTo(title.bottom, margin = 8.dp)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        width = Dimension.fillToConstraints
    }
    constrain(playModeButton) {
        top.linkTo(slider.bottom, margin = 16.dp)
        start.linkTo(parent.start)
        end.linkTo(prevButton.start)
        bottom.linkTo(guideBottom)
    }
    constrain(prevButton) {
        top.linkTo(slider.bottom, margin = 16.dp)
        start.linkTo(playModeButton.end)
        end.linkTo(playPauseButton.start)
        bottom.linkTo(playModeButton.bottom)
    }
    constrain(playPauseButton) {
        top.linkTo(slider.bottom, margin = 16.dp)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(playModeButton.bottom)
    }
    constrain(nextButton) {
        top.linkTo(slider.bottom, margin = 16.dp)
        start.linkTo(playPauseButton.end)
        end.linkTo(playlistButton.start)
        bottom.linkTo(playModeButton.bottom)
    }
    constrain(playlistButton) {
        top.linkTo(slider.bottom, margin = 16.dp)
        start.linkTo(nextButton.end)
        end.linkTo(parent.end)
        bottom.linkTo(playModeButton.bottom)
    }
}

private val LandscapeConstraints = ConstraintSet {
    val (artist, title, slider, playModeButton, prevButton, playPauseButton, nextButton, playlistButton) = createPlayerControlRefIds()

    val guideCenter = createGuidelineFromStart(0.5f)

    constrain(artist) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
    constrain(title) {
        top.linkTo(artist.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
    constrain(slider) {
        top.linkTo(title.bottom, margin = 8.dp)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        width = Dimension.fillToConstraints
    }
    constrain(prevButton) {
        top.linkTo(slider.bottom, margin = 20.dp)
        end.linkTo(playPauseButton.start, margin = 24.dp)
    }
    constrain(playPauseButton) {
        top.linkTo(slider.bottom, margin = 20.dp)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
    constrain(nextButton) {
        top.linkTo(slider.bottom, margin = 20.dp)
        start.linkTo(playPauseButton.end, margin = 24.dp)
    }
    constrain(playModeButton) {
        top.linkTo(playPauseButton.bottom, margin = 28.dp)
        end.linkTo(guideCenter, margin = 12.dp)
    }
    constrain(playlistButton) {
        top.linkTo(playPauseButton.bottom, margin = 28.dp)
        start.linkTo(guideCenter, margin = 12.dp)
    }
}

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    song: Song? = null,
    playbackState: PlaybackState,
    playMode: PlayMode,
    currentPosition: Long,
    duration: Long,
    onActionClick: (PlayerAction) -> Unit = {}
) {

    val isLandscape = rememberIsLandscape()
    val constraints = remember(isLandscape) {
        if (isLandscape) LandscapeConstraints else PortraitConstraints
    }

    val playbackIcon = if (playbackState == PlaybackState.PLAYING) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
    val playModeIcon = when (playMode) {
        PlayMode.REPEAT -> Icons.Rounded.Repeat
        PlayMode.SHUFFLE -> Icons.Rounded.Shuffle
        PlayMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
    }

    ConstraintLayout(
        constraintSet = constraints,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = song?.artist.orEmpty(),
            modifier = Modifier.layoutId("artist")
        )
        Text(
            text = song?.title.orEmpty(),
            modifier = Modifier.layoutId("title")
        )
        PlayerSlider(
            position = currentPosition,
            duration = duration,
            onSeek = { onActionClick(PlayerAction.SeekTo(it)) },
            modifier = Modifier.layoutId("slider")
        )
        IconAction(
            imageVector = playModeIcon,
            contentDescription = playModeIcon.name,
            onClick = { onActionClick(PlayerAction.TogglePlayMode) },
            modifier = Modifier.layoutId("playModeButton")
        )
        IconAction(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = Icons.Rounded.SkipPrevious.name,
            onClick = { onActionClick(PlayerAction.Previous) },
            modifier = Modifier.layoutId("prevButton")
        )
        IconAction(
            imageVector = playbackIcon,
            contentDescription = playbackIcon.name,
            onClick = { onActionClick(PlayerAction.TogglePlayPause) },
            modifier = Modifier.layoutId("playPauseButton")
        )
        IconAction(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = Icons.Rounded.SkipNext.name,
            onClick = { onActionClick(PlayerAction.Next) },
            modifier = Modifier.layoutId("nextButton")
        )
        IconAction(
            imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
            contentDescription = Icons.AutoMirrored.Rounded.PlaylistPlay.name,
            onClick = { onActionClick(PlayerAction.TogglePlayQueue) },
            modifier = Modifier.layoutId("playlistButton")
        )
    }
}
