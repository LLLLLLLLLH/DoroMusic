package com.doro.music.player.model

import com.doro.music.data.model.PlayMode

data class PersistedPlayState(
    val currentQueueId: Long = 0L,
    val currentSongId: Long = 0L,
    val positionMs: Long = 0L,
    val playMode: PlayMode = PlayMode.REPEAT,
    val shuffleSeed: Long = 0L
)
