package com.doro.music.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Playlist data model.
 * Using @Immutable annotation for Compose compiler optimization.
 */
@Immutable
@Serializable
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0
)
