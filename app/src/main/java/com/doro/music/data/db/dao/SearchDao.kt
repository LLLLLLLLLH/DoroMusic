package com.doro.music.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.doro.music.data.db.entities.SongEntity
import com.doro.music.data.model.SortMode
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {

    @Query(
        """
        SELECT * FROM songs 
        WHERE :keyword != '' AND (title LIKE '%' || :keyword || '%' OR artist LIKE '%' || :keyword || '%')
        ORDER BY 
            CASE :sortMode WHEN 'TITLE' THEN title END ASC,
            CASE :sortMode WHEN 'ARTIST' THEN artist END ASC,
            CASE :sortMode WHEN 'DATE_ADDED' THEN dateAdded END DESC
    """
    )
    fun getSongsByKeyWords(keyword: String, sortMode: SortMode): PagingSource<Int, SongEntity>

    @Query(
        """
        SELECT COUNT(*) FROM songs 
        WHERE :keyword != '' AND (title LIKE '%' || :keyword || '%' OR artist LIKE '%' || :keyword || '%')
    """
    )
    fun getSongCountByKeyWords(keyword: String): Flow<Int>

    @Query(
        """
        SELECT * FROM songs 
        WHERE :keyword != '' AND (title LIKE '%' || :keyword || '%' OR artist LIKE '%' || :keyword || '%')
        ORDER BY 
            CASE :sortMode WHEN 'TITLE' THEN title END ASC,
            CASE :sortMode WHEN 'ARTIST' THEN artist END ASC,
            CASE :sortMode WHEN 'DATE_ADDED' THEN dateAdded END DESC
    """
    )
    fun getAllSongsByKeyWords(keyword: String, sortMode: SortMode): Flow<List<SongEntity>>

}