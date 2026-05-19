package com.doro.music.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lyrics_cache",
    indices = [Index(value = ["song_id"], unique = true)]
)
data class LyricsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "song_id")
    val songId: Long,
    val lrcContent: String,
    val source: String,
    val fetchedAt: Long = System.currentTimeMillis(),
    val offset: Long = 0
)
