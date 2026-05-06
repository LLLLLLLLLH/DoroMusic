package com.doro.music.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import com.doro.music.data.db.entities.PlaylistEntity
import com.doro.music.data.db.entities.PlaylistWithSongCount
import com.doro.music.data.model.SortMode
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query(
        """
        SELECT p.*, COUNT(ps.songId) as songCount
        FROM playlists p
        LEFT JOIN playlist_songs ps ON p.id = ps.playlistId
        GROUP BY p.id
        ORDER BY 
            CASE :sortMode WHEN 'TITLE' THEN p.name END ASC,
            CASE :sortMode WHEN 'DATE_ADDED' THEN p.createdAt END DESC
    """
    )
    fun getPlaylistsSortedBy(sortMode: SortMode): PagingSource<Int, PlaylistWithSongCount>

    @Insert(onConflict = IGNORE)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM playlists WHERE name = :name)")
    fun isPlaylistNameExists(name: String): Boolean

    @Query("SELECT COUNT(*) FROM playlists")
    fun getPlaylistCount(): Flow<Int>
}
