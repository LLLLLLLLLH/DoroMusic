package com.doro.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.toSong
import com.doro.music.data.db.entities.toSongs
import com.doro.music.data.model.SortMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SongRepo(private val songDao: SongDao) {

    private val pagingConfig = PagingConfig(pageSize = 15, enablePlaceholders = false)

    fun getSongs(sortMode: SortMode) = Pager(config = pagingConfig) {
        songDao.getSongsSortedBy(sortMode)
    }.flow.map { pagingData -> pagingData.map { it.toSong() } }

    fun getSongCount() = songDao.getSongCount()

    suspend fun getAllSongs(sortMode: SortMode) = withContext(Dispatchers.IO) {
        songDao.getAllSongsSortedBy(sortMode).toSongs()
    }

}
