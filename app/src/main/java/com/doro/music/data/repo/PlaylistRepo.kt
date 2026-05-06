package com.doro.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.doro.music.data.db.dao.PlaylistDao
import com.doro.music.data.db.entities.PlaylistEntity
import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.db.entities.PlaylistSongEntity
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.SortMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistRepo(
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao,
) {

    private val pagingConfig = PagingConfig(pageSize = 15, enablePlaceholders = false)

    fun getPlaylists(sortMode: SortMode) = Pager(config = pagingConfig) {
        playlistDao.getPlaylistsSortedBy(sortMode)
    }.flow.map { pagingData ->
        pagingData.map { Playlist(id = it.playlist.id, name = it.playlist.name, songCount = it.songCount) }
    }

    fun getPlaylistCount(): Flow<Int> = playlistDao.getPlaylistCount()

    suspend fun createPlaylist(name: String) = withContext(Dispatchers.IO) {
        val rowId = playlistDao.createPlaylist(PlaylistEntity(name = name))
        rowId != -1L
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        val rowCount = playlistDao.deletePlaylist(playlistId)
        rowCount > 0
    }

    suspend fun addSongToPlaylist(playlists: List<Playlist>, songId: Long): AddSongResult = withContext(Dispatchers.IO) {
        val entities = playlists.map { PlaylistSongEntity(playlistId = it.id, songId = songId) }
        val rowIds = playlistSongDao.addSongToPlaylist(entities)
        val addedCount = rowIds.count { it != -1L }
        val existedCount = rowIds.count { it == -1L }
        when {
            addedCount > 0 && existedCount >= 0 -> AddSongResult.Success
            addedCount == 0 && existedCount > 0 -> AddSongResult.AlreadyExists
            else -> AddSongResult.Failed
        }
    }

    suspend fun isPlaylistNameExists(name: String): Boolean = withContext(Dispatchers.IO) {
        playlistDao.isPlaylistNameExists(name)
    }
}
