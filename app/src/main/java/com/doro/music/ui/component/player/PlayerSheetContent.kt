package com.doro.music.ui.component.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlaybackState
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.Song
import com.doro.music.ui.component.SheetProgressState

@Composable
fun PlayerSheetContent(
    modifier: Modifier = Modifier,
    sheetProgressState: SheetProgressState,
    playerViewType: PlayerViewType,
    song: Song? = null,
    playbackState: PlaybackState,
    playMode: PlayMode,
    currentPosition: Long,
    onActionClick: (PlayerAction) -> Unit = {}
) {
    if (playbackState == PlaybackState.IDLE) return

    Column(
        modifier = modifier
    ) {
        MiniPlayer(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetProgressState.peekHeight),
            sheetProgressState = sheetProgressState,
            onActionClick = onActionClick,
            song = song,
            playbackState = playbackState
        )

        FullPlayer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            sheetProgressState = sheetProgressState,
            playerViewType = playerViewType,
            song = song,
            playbackState = playbackState,
            playMode = playMode,
            currentPosition = currentPosition,
            onActionClick = onActionClick
        )
    }
}
