package com.doro.music.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_queue",
    indices = [
        Index(value = ["sort_order"]),
        Index(value = ["shuffle_order"]),
        Index(value = ["song_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlayerQueueEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("queue_id") val queueId: Long = 0,
    @ColumnInfo("song_id") val songId: Long,
    @ColumnInfo("sort_order") val sortOrder: String,
    @ColumnInfo("shuffle_order") val shuffleOrder: String = "z"
)