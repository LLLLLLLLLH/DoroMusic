package com.doro.music.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doro.music.data.db.entities.PlayQueueEntity
import com.doro.music.data.db.entities.SongEntity

@Dao
interface PlayQueueDao {

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN play_queue pq ON s.id = pq.songId
        ORDER BY pq.id ASC
    """)
    suspend fun getPlayQueueSongs(): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongsToQueue(songs: List<PlayQueueEntity>)

    @Query("DELETE FROM play_queue")
    suspend fun clearQueue()
}
