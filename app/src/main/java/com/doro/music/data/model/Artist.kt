package com.doro.music.data.model

import androidx.room.ColumnInfo
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
     val name: String,
     val songCount: Int = 0
) {
    override fun toString(): String = name
}
