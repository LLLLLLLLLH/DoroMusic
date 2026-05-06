package com.doro.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.doro.music.data.db.dao.SearchDao
import com.doro.music.data.db.entities.toSong
import com.doro.music.data.db.entities.toSongs
import com.doro.music.data.model.SortMode
import kotlinx.coroutines.flow.map

class SearchRepo(private val searchDao: SearchDao) {

    fun getSongsByKeyWords(keyword: String, sortMode: SortMode) = Pager(
        config = PagingConfig(pageSize = 15, enablePlaceholders = false)
    ) { searchDao.getSongsByKeyWords(keyword, sortMode) }.flow.map { pagingData -> pagingData.map { it.toSong() } }

    fun getSongCountByKeyWords(keyword: String) = searchDao.getSongCountByKeyWords(keyword)

    fun getAllSongsByKeyWords(keyword: String, sortMode: SortMode) = searchDao.getAllSongsByKeyWords(keyword, sortMode).map { it.toSongs() }

}

