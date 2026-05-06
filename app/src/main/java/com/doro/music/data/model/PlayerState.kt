package com.doro.music.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val currentIndex: Int,
    val currentPosition: Long
)
