package com.doro.music.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Song(
    val id: Long = 0,
    val uri: String? = null,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val albumArt: String? = null,
    val year: Int? = null,
    val duration: Long = 0,
    val mimeType: String? = null,
    val path: String,
    val genre: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val size: Long = 0
)
