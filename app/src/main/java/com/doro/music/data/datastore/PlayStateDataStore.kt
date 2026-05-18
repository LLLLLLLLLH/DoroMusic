package com.doro.music.data.datastore

import com.doro.music.data.model.PlayMode
import com.doro.music.player.model.PersistedPlayState
import kotlinx.coroutines.flow.Flow

interface PlayStateDataStore {
    val currentQueueId: Flow<Long>
    val currentSongId: Flow<Long>
    val currentPositionMs: Flow<Long>
    val playMode: Flow<PlayMode>
    val shuffleSeed: Flow<Long>
    val persistentState: Flow<PersistedPlayState>
    suspend fun saveCurrentQueueId(queueId: Long)
    suspend fun saveCurrentSongId(songId: Long)
    suspend fun saveCurrentPosition(positionMs: Long)
    suspend fun savePlayMode(mode: PlayMode)
    suspend fun saveShuffleSeed(seed: Long)
    suspend fun clearState()
}