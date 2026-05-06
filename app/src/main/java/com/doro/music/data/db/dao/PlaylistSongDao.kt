package com.doro.music.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import com.doro.music.data.db.entities.PlaylistSongEntity
import com.doro.music.data.db.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.addedAt DESC
    """)
    fun getSongsByPlaylist(playlistId: Long): PagingSource<Int, SongEntity>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.addedAt DESC
    """)
    fun getAllSongsByPlaylist(playlistId: Long): List<SongEntity>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCountByPlaylist(playlistId: Long): Flow<Int>

    @Insert(onConflict = IGNORE)
    suspend fun addSongToPlaylist(playlistSong: List<PlaylistSongEntity>): List<Long>

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long): Int

}
