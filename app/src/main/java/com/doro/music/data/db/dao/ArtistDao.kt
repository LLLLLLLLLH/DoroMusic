package com.doro.music.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.doro.music.data.db.entities.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("""
        SELECT artist as name, COUNT(*) as songCount
        FROM songs
        WHERE artist IS NOT NULL AND artist != ''
        GROUP BY artist
        ORDER BY artist ASC
    """)
    fun getArtists(): PagingSource<Int, ArtistEntity>

    @Query("""
        SELECT COUNT(DISTINCT artist)
        FROM songs
        WHERE artist IS NOT NULL AND artist != ''
    """)
    fun getArtistCount(): Flow<Int>
}
