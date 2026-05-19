package com.doro.music.ui.component.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.doro.music.data.model.PlaybackState
import com.doro.music.ui.component.SheetProgressState
import com.doro.music.vm.PlayerViewModel

/**
 * 播放器 Sheet 内容
 *
 * 自行从 PlayerViewModel 订阅播放器状态，避免上层 MainRoute 因高频状态（如 currentPosition）
 * 而反复重组。
 */
@Composable
fun PlayerSheetContent(
    vm: PlayerViewModel,
    sheetProgressState: SheetProgressState,
    modifier: Modifier = Modifier
) {
    val playerViewType by vm.playerViewType.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val currentSong by vm.currentSong.collectAsStateWithLifecycle()
    val currentPosition by vm.currentPosition.collectAsStateWithLifecycle()
    val currentLyrics by vm.currentLyrics.collectAsStateWithLifecycle()
    val currentLyricIndex by vm.currentLyricIndex.collectAsStateWithLifecycle()

    if (uiState.playbackState == PlaybackState.IDLE) return

    Column(
        modifier = modifier
    ) {
        MiniPlayer(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetProgressState.peekHeight),
            sheetProgressState = sheetProgressState,
            onActionClick = vm::handlePlayerAction,
            song = currentSong,
            playbackState = uiState.playbackState
        )

        FullPlayer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            sheetProgressState = sheetProgressState,
            playerViewType = playerViewType,
            song = currentSong,
            playbackState = uiState.playbackState,
            playMode = uiState.playMode,
            currentPosition = currentPosition,
            lyrics = currentLyrics,
            lyricIndex = currentLyricIndex,
            onActionClick = vm::handlePlayerAction
        )
    }
}
