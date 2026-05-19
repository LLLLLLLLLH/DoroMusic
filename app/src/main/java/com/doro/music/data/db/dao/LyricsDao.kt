package com.doro.music.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doro.music.data.db.entities.LyricsEntity

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics_cache WHERE song_id = :songId LIMIT 1")
    suspend fun getBySongId(songId: Long): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LyricsEntity)

}
