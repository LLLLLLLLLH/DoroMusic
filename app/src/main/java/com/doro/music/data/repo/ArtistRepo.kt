package com.doro.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.doro.music.data.db.dao.ArtistDao
import com.doro.music.data.db.entities.toArtist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArtistRepo(private val artistDao: ArtistDao) {

    private val pagingConfig = PagingConfig(pageSize = 15, enablePlaceholders = false)

    fun getArtists() = Pager(config = pagingConfig) { artistDao.getArtists() }.flow
        .map { pagingData -> pagingData.map { it.toArtist() } }

    fun getArtistCount(): Flow<Int> = artistDao.getArtistCount()
}
