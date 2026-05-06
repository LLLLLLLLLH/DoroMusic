package com.doro.music.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.doro.music.data.db.entities.SongEntity
import com.doro.music.data.model.SortMode
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query(
        """
        SELECT * FROM songs 
        ORDER BY 
            CASE :sortMode WHEN 'TITLE' THEN title END ASC,
            CASE :sortMode WHEN 'ARTIST' THEN artist END ASC,
            CASE :sortMode WHEN 'DATE_ADDED' THEN dateAdded END DESC
    """
    )
    fun getSongsSortedBy(sortMode: SortMode): PagingSource<Int, SongEntity>

    @Query(
        """
        SELECT * FROM songs 
        ORDER BY 
            CASE :sortMode WHEN 'TITLE' THEN title END ASC,
            CASE :sortMode WHEN 'ARTIST' THEN artist END ASC,
            CASE :sortMode WHEN 'DATE_ADDED' THEN dateAdded END DESC
    """
    )
    suspend fun getAllSongsSortedBy(sortMode: SortMode): List<SongEntity>

    @Upsert
    suspend fun saveSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE path IN (:paths)")
    suspend fun deleteByPaths(paths: List<String>)

    @Transaction
    suspend fun syncSongs(songs: List<SongEntity>) {
        val newPaths = songs.map { it.path }.toSet()
        val existingPaths = getAllSongPaths().toSet()
        val stalePaths = existingPaths - newPaths
        if (stalePaths.isNotEmpty()) {
            deleteByPaths(stalePaths.toList())
        }
        saveSongs(songs)
    }

    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Flow<Int>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title ASC")
    fun getSongsByArtist(artist: String): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title ASC")
    fun getAllSongsByArtist(artist: String): List<SongEntity>

    @Query("SELECT path FROM songs WHERE path IS NOT NULL AND path != ''")
    suspend fun getAllSongPaths(): List<String>

    @Query("SELECT * FROM songs WHERE path LIKE :folderPath || '/%' ORDER BY title ASC")
    fun getSongsByFolder(folderPath: String): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE path LIKE :folderPath || '/%' ORDER BY title ASC")
    suspend fun getAllSongsByFolder(folderPath: String): List<SongEntity>

    @Query("SELECT COUNT(*) FROM songs WHERE artist = :name")
    fun getSongCountByArtist(name: String): Flow<Int>
}
