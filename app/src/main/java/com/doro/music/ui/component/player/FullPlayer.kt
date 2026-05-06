package com.doro.music.ui.component.player

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.PlaybackState
import com.doro.music.data.model.Song
import com.doro.music.ui.component.SheetProgressState

private val PortraitConstraints = ConstraintSet {
    val collapseButton = createRefFor("collapseButton")
    val display = createRefFor("display")
    val controls = createRefFor("controls")

    constrain(collapseButton) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
    }
    constrain(display) {
        top.linkTo(collapseButton.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(controls.top)
        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
    }
    constrain(controls) {
        bottom.linkTo(parent.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        height = Dimension.wrapContent
        width = Dimension.fillToConstraints
    }
}

private val LandscapeConstraints = ConstraintSet {
    val collapseButton = createRefFor("collapseButton")
    val display = createRefFor("display")
    val controls = createRefFor("controls")

    constrain(collapseButton) {
        top.linkTo(parent.top)
        end.linkTo(parent.end)
        start.linkTo(controls.end)
    }
    constrain(display) {
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        start.linkTo(parent.start)
        end.linkTo(controls.start)
        width = Dimension.percent(0.3f)
    }
    constrain(controls) {
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        start.linkTo(display.end)
        end.linkTo(parent.end)
        width = Dimension.percent(0.3f)
    }
}

@Composable
fun FullPlayer(
    modifier: Modifier = Modifier,
    sheetProgressState: SheetProgressState,
    playerViewType: PlayerViewType,
    song: Song? = null,
    playbackState: PlaybackState,
    playMode: PlayMode,
    currentPosition: Long,
    duration: Long,
    onActionClick: (PlayerAction) -> Unit = {}
) {
    val isLandscape = rememberIsLandscape()
    val constraints = remember(isLandscape) { if (isLandscape) LandscapeConstraints else PortraitConstraints }

    ConstraintLayout(
        constraintSet = constraints,
        modifier = modifier.graphicsLayer { this.alpha = sheetProgressState.progress }
    ) {
        IconButton(
            onClick = { onActionClick(PlayerAction.TogglePlayerSheet) },
            modifier = Modifier.layoutId("collapseButton")
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = Icons.Rounded.KeyboardArrowDown.name
            )
        }
        PlayerDisplay(
            modifier = Modifier.layoutId("display"),
            song = song,
            playbackState = playbackState,
            currentView = playerViewType,
            onActionClick = onActionClick
        )
        PlayerControls(
            modifier = Modifier.layoutId("controls"),
            song = song,
            playbackState = playbackState,
            playMode = playMode,
            currentPosition = currentPosition,
            duration = duration,
            onActionClick = onActionClick
        )
    }
}
