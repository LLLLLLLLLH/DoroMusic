package com.doro.music.data.model


sealed interface PlayerAction {
    data object Previous : PlayerAction
    data object TogglePlayPause : PlayerAction
    data object Next : PlayerAction
    data object TogglePlayMode : PlayerAction
    data object TogglePlayerView : PlayerAction
    data object TogglePlayerSheet : PlayerAction
    data object TogglePlayQueue : PlayerAction
    data class SeekTo(val positionMs: Long) : PlayerAction
}
