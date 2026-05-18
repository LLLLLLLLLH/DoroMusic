package com.doro.music.data.repo

import androidx.paging.PagingData
import com.doro.music.data.model.PlayMode
import com.doro.music.player.model.PlayContext
import com.doro.music.player.model.QueueSong
import kotlinx.coroutines.flow.Flow

interface QueueWriteOps {
    suspend fun playNewQueue(songIds: List<Long>, targetSongId: Long, playMode: PlayMode): Long?
    suspend fun insertNext(currentQueueId: Long, songIdsToInsert: List<Long>)
    suspend fun switchToShuffle(currentQueueId: Long)
    suspend fun switchToSequential()
    suspend fun switchToRepeatOne()
    suspend fun removeByQueueId(queueId: Long)
    suspend fun swapItems(queueId1: Long, queueId2: Long, playMode: PlayMode)
    suspend fun appendToQueue(songIds: List<Long>)
    suspend fun reassignShuffleOrdersWithSeed(anchorQueueId: Long, seed: Long)
    suspend fun getQueueSize(): Int
    suspend fun getSongIdByQueueId(queueId: Long): Long?
    suspend fun getQueueSongById(queueId: Long): QueueSong?
    suspend fun getOrder(queueId: Long, playMode: PlayMode): String?
    suspend fun getNextQueueId(currentOrder: String, playMode: PlayMode): Long?
    suspend fun getPrevQueueId(currentOrder: String, playMode: PlayMode): Long?
    suspend fun getFirstQueueId(playMode: PlayMode): Long?
    suspend fun getLastQueueId(playMode: PlayMode): Long?
    suspend fun resolveSongIds(context: PlayContext): List<Long>
}

interface QueueReadOps {
    fun getPagedPlaybackQueue(): Flow<PagingData<QueueSong>>
}
