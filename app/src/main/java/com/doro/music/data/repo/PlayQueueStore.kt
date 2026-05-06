package com.doro.music.data.repo

import com.doro.music.data.model.Song

interface PlayQueueStore {
    suspend fun save(songIds: List<Long>)
    suspend fun load(): List<Song>
}
