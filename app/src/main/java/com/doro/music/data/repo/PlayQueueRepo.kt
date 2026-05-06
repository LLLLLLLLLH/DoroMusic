package com.doro.music.data.repo

import com.doro.music.data.db.dao.PlayQueueDao
import com.doro.music.data.db.entities.PlayQueueEntity
import com.doro.music.data.db.entities.toSong
import com.doro.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlayQueueRepo(
    private val playQueueDao: PlayQueueDao
) : PlayQueueStore {

    override suspend fun load(): List<Song> = withContext(Dispatchers.IO) {
        playQueueDao.getPlayQueueSongs().map { it.toSong() }
    }

    override suspend fun save(songIds: List<Long>) = withContext(Dispatchers.IO) {
        playQueueDao.clearQueue()
        val entities = songIds.map { PlayQueueEntity(songId = it) }
        playQueueDao.addSongsToQueue(entities)
    }

}
