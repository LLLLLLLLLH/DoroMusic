package com.doro.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.doro.music.data.db.dao.FolderDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.toFolders
import com.doro.music.data.db.entities.toSong
import com.doro.music.data.model.Folder
import com.doro.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FolderRepo(
    private val songDao: SongDao,
    private val folderDao: FolderDao
) {

    private val pagingConfig = PagingConfig(pageSize = 15, enablePlaceholders = false)

    val folders: Flow<List<Folder>> = folderDao.getAllFolders().map { it.toFolders() }

    fun getSongsByFolder(folderPath: String) = Pager(config = pagingConfig) {
        songDao.getSongsByFolder(folderPath)
    }.flow.map { pagingData -> pagingData.map { it.toSong() } }

    suspend fun getAllSongsByFolder(folderPath: String): List<Song> = withContext(Dispatchers.IO) {
        songDao.getAllSongsByFolder(folderPath).map { it.toSong() }
    }
}
