package com.doro.music.player.model

import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlaybackState

data class PlayUiState(
    val currentQueueId: Long? = null,
    val currentSongId: Long? = null,
    val playMode: PlayMode = PlayMode.REPEAT,
    val playbackState: PlaybackState = PlaybackState.IDLE
) {
    companion object {
        val Empty = PlayUiState()
    }
}
