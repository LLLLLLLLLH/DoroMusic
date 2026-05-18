@file:OptIn(ExperimentalCoroutinesApi::class)

package com.doro.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.doro.music.data.db.dao.PlayQueueDao
import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.db.dao.SearchDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.model.PlayMode
import com.doro.music.player.model.PlayContext
import com.doro.music.data.db.entities.PlayerQueueEntity
import com.doro.music.player.model.QueueSong
import com.doro.music.data.datastore.PlayStateDataStore
import com.doro.music.player.util.FractionalIndexer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import java.util.Random

class PlayQueueRepo(
    private val queueDao: PlayQueueDao,
    private val songDao: SongDao,
    private val playlistSongDao: PlaylistSongDao,
    private val searchDao: SearchDao,
    private val store: PlayStateDataStore
) : QueueWriteOps, QueueReadOps {

    private companion object {
        /** 默认 shuffle 排序键，表示未分配随机顺序 */
        const val DEFAULT_SHUFFLE_ORDER = "z"
    }

    override suspend fun playNewQueue(songIds: List<Long>, targetSongId: Long, playMode: PlayMode): Long? {
        queueDao.clearQueue()
        val orders = FractionalIndexer.generateInitialList(songIds.size)
        val entities = songIds.mapIndexed { index, songId ->
            PlayerQueueEntity(songId = songId, sortOrder = orders[index], shuffleOrder = DEFAULT_SHUFFLE_ORDER)
        }
        queueDao.insertAll(entities)
        val targetQueueId = queueDao.getQueueIdBySongId(targetSongId) ?: return null
        assignShuffleOrders(targetQueueId, System.currentTimeMillis())
        return targetQueueId
    }

    override suspend fun insertNext(currentQueueId: Long, songIdsToInsert: List<Long>) {
        val currentSortOrder = queueDao.getSortOrder(currentQueueId) ?: return
        val nextSortOrder = queueDao.getNextSortOrder(currentSortOrder)
        val sortInsertOrders = FractionalIndexer.generateBetweenList(
            before = currentSortOrder, after = nextSortOrder, count = songIdsToInsert.size
        )

        val shuffleInsertOrders = if (store.playMode.first() == PlayMode.SHUFFLE) {
            val currentShuffleOrder = queueDao.getShuffleOrder(currentQueueId) ?: return
            val nextShuffleOrder = queueDao.getNextShuffleOrder(currentShuffleOrder)
            FractionalIndexer.generateBetweenList(
                before = currentShuffleOrder, after = nextShuffleOrder, count = songIdsToInsert.size
            )
        } else {
            List(songIdsToInsert.size) { DEFAULT_SHUFFLE_ORDER }
        }

        val entities = songIdsToInsert.mapIndexed { index, songId ->
            PlayerQueueEntity(
                songId = songId,
                sortOrder = sortInsertOrders[index],
                shuffleOrder = shuffleInsertOrders[index]
            )
        }
        queueDao.insertAll(entities)
    }

    override suspend fun switchToShuffle(currentQueueId: Long) {
        val allQueueIds = queueDao.getAllQueueIdsSorted()
        val seed = System.currentTimeMillis()
        val others = allQueueIds.filter { it != currentQueueId }.shuffled(Random(seed))
        val orders = FractionalIndexer.generateInitialList(allQueueIds.size)
        queueDao.updateShuffleOrder(currentQueueId, orders[0])
        others.forEachIndexed { index, queueId ->
            queueDao.updateShuffleOrder(queueId, orders[index + 1])
        }
    }

    override suspend fun switchToSequential() {}
    override suspend fun switchToRepeatOne() {}
    override suspend fun removeByQueueId(queueId: Long) = queueDao.removeByQueueId(queueId)

    override suspend fun swapItems(queueId1: Long, queueId2: Long, playMode: PlayMode) {
        if (playMode == PlayMode.SHUFFLE) {
            val order1 = queueDao.getShuffleOrder(queueId1) ?: return
            val order2 = queueDao.getShuffleOrder(queueId2) ?: return
            queueDao.updateShuffleOrder(queueId1, order2)
            queueDao.updateShuffleOrder(queueId2, order1)
        } else {
            val order1 = queueDao.getSortOrder(queueId1) ?: return
            val order2 = queueDao.getSortOrder(queueId2) ?: return
            queueDao.updateSortOrder(queueId1, order2)
            queueDao.updateSortOrder(queueId2, order1)
        }
    }

    override suspend fun appendToQueue(songIds: List<Long>) {
        val maxSortOrder = queueDao.getMaxSortOrder() ?: "a0"
        val orders = FractionalIndexer.generateBetweenList(
            before = maxSortOrder, after = null, count = songIds.size
        )
        val entities = songIds.mapIndexed { index, songId ->
            PlayerQueueEntity(songId = songId, sortOrder = orders[index], shuffleOrder = DEFAULT_SHUFFLE_ORDER)
        }
        queueDao.insertAll(entities)
    }

    override suspend fun reassignShuffleOrdersWithSeed(anchorQueueId: Long, seed: Long) {
        assignShuffleOrders(anchorQueueId, seed)
    }

    override suspend fun getQueueSize(): Int = queueDao.getQueueSize()
    override suspend fun getSongIdByQueueId(queueId: Long): Long? = queueDao.getSongIdByQueueId(queueId)
    override suspend fun getQueueSongById(queueId: Long): QueueSong? = queueDao.getQueueSongById(queueId)

    override suspend fun getOrder(queueId: Long, playMode: PlayMode): String? {
        return if (playMode == PlayMode.SHUFFLE) queueDao.getShuffleOrder(queueId)
        else queueDao.getSortOrder(queueId)
    }

    override suspend fun getNextQueueId(currentOrder: String, playMode: PlayMode): Long? {
        val nextOrder = if (playMode == PlayMode.SHUFFLE) queueDao.getNextShuffleOrder(currentOrder)
        else queueDao.getNextSortOrder(currentOrder)
        if (nextOrder == null) return null
        return resolveQueueIdByOrder(nextOrder, playMode)
    }

    override suspend fun getPrevQueueId(currentOrder: String, playMode: PlayMode): Long? {
        val prevOrder = if (playMode == PlayMode.SHUFFLE) queueDao.getPrevShuffleOrder(currentOrder)
        else queueDao.getPrevSortOrder(currentOrder)
        if (prevOrder == null) return null
        return resolveQueueIdByOrder(prevOrder, playMode)
    }

    override suspend fun getFirstQueueId(playMode: PlayMode): Long? {
        val order = if (playMode == PlayMode.SHUFFLE) queueDao.getMinShuffleOrder()
        else queueDao.getMinSortOrder()
        if (order == null) return null
        return resolveQueueIdByOrder(order, playMode)
    }

    override suspend fun getLastQueueId(playMode: PlayMode): Long? {
        val order = if (playMode == PlayMode.SHUFFLE) queueDao.getMaxShuffleOrder()
        else queueDao.getMaxSortOrder()
        if (order == null) return null
        return resolveQueueIdByOrder(order, playMode)
    }

    override suspend fun resolveSongIds(context: PlayContext): List<Long> {
        return when (context) {
            is PlayContext.All -> songDao.getAllSongsSortedBy(context.sortMode).map { it.id }
            is PlayContext.Artist -> songDao.getAllSongsByArtist(context.artist).map { it.id }
            is PlayContext.Folder -> songDao.getAllSongsByFolder(context.path).map { it.id }
            is PlayContext.Playlist -> playlistSongDao.getAllSongsByPlaylist(context.playlistId).map { it.id }
            is PlayContext.Search -> searchDao.getAllSongsByKeyWords(context.keyword, context.sortMode).first().map { it.id }
        }
    }

    override fun getPagedPlaybackQueue() = store.playMode.flatMapLatest { mode ->
        Pager(config = PagingConfig(pageSize = 15), pagingSourceFactory = {
            when (mode) {
                PlayMode.SHUFFLE -> queueDao.getPlayQueueShuffled()
                else -> queueDao.getPlayQueueSorted()
            }
        }).flow
    }

    private suspend fun assignShuffleOrders(anchorQueueId: Long, seed: Long) {
        val allQueueIds = queueDao.getAllQueueIdsSorted()
        val anchorInList = anchorQueueId in allQueueIds
        val others = allQueueIds.filter { it != anchorQueueId }.shuffled(Random(seed))
        val totalSize = if (anchorInList) allQueueIds.size else allQueueIds.size + 1
        val orders = FractionalIndexer.generateInitialList(totalSize)
        queueDao.updateShuffleOrder(anchorQueueId, orders[0])
        others.forEachIndexed { index, queueId ->
            queueDao.updateShuffleOrder(queueId, orders[index + 1])
        }
    }

    private suspend fun resolveQueueIdByOrder(order: String, playMode: PlayMode): Long? {
        return if (playMode == PlayMode.SHUFFLE) queueDao.getQueueIdByExactShuffleOrder(order)
        else queueDao.getQueueIdByExactSortOrder(order)
    }
}