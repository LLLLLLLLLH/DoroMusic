package com.doro.music.ui.component.player

sealed interface PlayerSheetState {
    data object Hidden : PlayerSheetState
    data object Collapsed : PlayerSheetState
    data object Expanded : PlayerSheetState
}
