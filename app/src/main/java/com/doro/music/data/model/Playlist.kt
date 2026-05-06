package com.doro.music.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0
)
