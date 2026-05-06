package com.doro.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.toSong
import com.doro.music.data.db.entities.toSongs
import com.doro.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SongListRepo(
    private val songDao: SongDao,
    private val playlistSongDao: PlaylistSongDao,
) {

    private val pagingConfig = PagingConfig(pageSize = 15, enablePlaceholders = false)

    fun getSongListByArtist(name: String) = Pager(config = pagingConfig) { songDao.getSongsByArtist(name) }.flow.map { data -> data.map { it.toSong() } }

    fun getSongListByPlaylist(id: Long) = Pager(config = pagingConfig) { playlistSongDao.getSongsByPlaylist(id) }.flow.map { data -> data.map { it.toSong() } }

    fun getSongCountByArtist(name: String) = songDao.getSongCountByArtist(name)

    fun getSongCountByPlaylist(id: Long) = playlistSongDao.getSongCountByPlaylist(id)

    suspend fun getAllSongsByArtist(name: String): List<Song> = withContext(Dispatchers.IO) {
        songDao.getAllSongsByArtist(name).toSongs()
    }

    suspend fun getAllSongsByPlaylist(id: Long): List<Song> = withContext(Dispatchers.IO) {
        playlistSongDao.getAllSongsByPlaylist(id).toSongs()
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        val rowCount = playlistSongDao.removeSongFromPlaylist(playlistId, songId)
        rowCount > 0
    }
}
