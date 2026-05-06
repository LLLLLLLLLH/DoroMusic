package com.doro.music.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_queue")
data class PlayQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long
)
