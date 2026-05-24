package com.doro.music.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Artist(
    val name: String,
    val songCount: Int = 0
) {
    override fun toString(): String = name
}
