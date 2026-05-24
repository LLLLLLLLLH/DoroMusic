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
        const val DEFAULT_SHUFFLE_ORDER = "z"
    }

    private inner class OrderAccess(private val mode: PlayMode) {
        suspend fun getOrder(queueId: Long) = if (mode == PlayMode.SHUFFLE) queueDao.getShuffleOrder(queueId) else queueDao.getSortOrder(queueId)
        suspend fun getNextOrder(current: String) = if (mode == PlayMode.SHUFFLE) queueDao.getNextShuffleOrder(current) else queueDao.getNextSortOrder(current)
        suspend fun getPrevOrder(current: String) = if (mode == PlayMode.SHUFFLE) queueDao.getPrevShuffleOrder(current) else queueDao.getPrevSortOrder(current)
        suspend fun getMinOrder() = if (mode == PlayMode.SHUFFLE) queueDao.getMinShuffleOrder() else queueDao.getMinSortOrder()
        suspend fun getMaxOrder() = if (mode == PlayMode.SHUFFLE) queueDao.getMaxShuffleOrder() else queueDao.getMaxSortOrder()
        suspend fun getQueueIdByOrder(order: String) = if (mode == PlayMode.SHUFFLE) queueDao.getQueueIdByExactShuffleOrder(order) else queueDao.getQueueIdByExactSortOrder(order)
        suspend fun updateOrder(queueId: Long, newOrder: String) = if (mode == PlayMode.SHUFFLE) queueDao.updateShuffleOrder(queueId, newOrder) else queueDao.updateSortOrder(queueId, newOrder)
    }

    private fun PlayMode.orderAccess() = OrderAccess(this)

    // ==================== QueueWriteOps ====================

    override suspend fun playNewQueue(songIds: List<Long>, targetSongId: Long, playMode: PlayMode): Long? {
        queueDao.clearQueue()
        val orders = FractionalIndexer.generateInitialList(songIds.size)
        queueDao.insertAll(songIds.mapIndexed { index, songId ->
            PlayerQueueEntity(songId = songId, sortOrder = orders[index], shuffleOrder = DEFAULT_SHUFFLE_ORDER)
        })
        val targetQueueId = queueDao.getQueueIdBySongId(targetSongId) ?: return null
        assignShuffleOrders(targetQueueId, System.currentTimeMillis())
        return targetQueueId
    }

    override suspend fun insertNext(currentQueueId: Long, songIdsToInsert: List<Long>) {
        val currentSortOrder = queueDao.getSortOrder(currentQueueId) ?: return
        val sortInsertOrders = FractionalIndexer.generateBetweenList(
            before = currentSortOrder, after = queueDao.getNextSortOrder(currentSortOrder), count = songIdsToInsert.size
        )
        val shuffleInsertOrders = if (store.playMode.first() == PlayMode.SHUFFLE) {
            val currentShuffleOrder = queueDao.getShuffleOrder(currentQueueId) ?: return
            FractionalIndexer.generateBetweenList(
                before = currentShuffleOrder, after = queueDao.getNextShuffleOrder(currentShuffleOrder), count = songIdsToInsert.size
            )
        } else {
            List(songIdsToInsert.size) { DEFAULT_SHUFFLE_ORDER }
        }
        queueDao.insertAll(songIdsToInsert.mapIndexed { index, songId ->
            PlayerQueueEntity(songId = songId, sortOrder = sortInsertOrders[index], shuffleOrder = shuffleInsertOrders[index])
        })
    }

    override suspend fun switchToShuffle(currentQueueId: Long) {
        val allQueueIds = queueDao.getAllQueueIdsSorted()
        val others = allQueueIds.filter { it != currentQueueId }.shuffled(Random(System.currentTimeMillis()))
        val orders = FractionalIndexer.generateInitialList(allQueueIds.size)
        queueDao.updateShuffleOrder(currentQueueId, orders[0])
        others.forEachIndexed { index, queueId -> queueDao.updateShuffleOrder(queueId, orders[index + 1]) }
    }

    override suspend fun switchToSequential() {}
    override suspend fun switchToRepeatOne() {}
    override suspend fun removeByQueueId(queueId: Long) = queueDao.removeByQueueId(queueId)

    override suspend fun swapItems(queueId1: Long, queueId2: Long, playMode: PlayMode) {
        val access = playMode.orderAccess()
        val order1 = access.getOrder(queueId1) ?: return
        val order2 = access.getOrder(queueId2) ?: return
        access.updateOrder(queueId1, order2)
        access.updateOrder(queueId2, order1)
    }

    override suspend fun appendToQueue(songIds: List<Long>) {
        val orders = FractionalIndexer.generateBetweenList(
            before = queueDao.getMaxSortOrder() ?: "a0", after = null, count = songIds.size
        )
        queueDao.insertAll(songIds.mapIndexed { index, songId ->
            PlayerQueueEntity(songId = songId, sortOrder = orders[index], shuffleOrder = DEFAULT_SHUFFLE_ORDER)
        })
    }

    override suspend fun reassignShuffleOrdersWithSeed(anchorQueueId: Long, seed: Long) {
        assignShuffleOrders(anchorQueueId, seed)
    }

    // ==================== QueueReadOps ====================

    override suspend fun getQueueSize(): Int = queueDao.getQueueSize()
    override suspend fun getSongIdByQueueId(queueId: Long): Long? = queueDao.getSongIdByQueueId(queueId)
    override suspend fun getQueueSongById(queueId: Long): QueueSong? = queueDao.getQueueSongById(queueId)

    override suspend fun getOrder(queueId: Long, playMode: PlayMode) = playMode.orderAccess().getOrder(queueId)

    override suspend fun getNextQueueId(currentOrder: String, playMode: PlayMode): Long? {
        return playMode.orderAccess().getNextOrder(currentOrder)?.let { playMode.orderAccess().getQueueIdByOrder(it) }
    }

    override suspend fun getPrevQueueId(currentOrder: String, playMode: PlayMode): Long? {
        return playMode.orderAccess().getPrevOrder(currentOrder)?.let { playMode.orderAccess().getQueueIdByOrder(it) }
    }

    override suspend fun getFirstQueueId(playMode: PlayMode): Long? {
        return playMode.orderAccess().getMinOrder()?.let { playMode.orderAccess().getQueueIdByOrder(it) }
    }

    override suspend fun getLastQueueId(playMode: PlayMode): Long? {
        return playMode.orderAccess().getMaxOrder()?.let { playMode.orderAccess().getQueueIdByOrder(it) }
    }

    override suspend fun resolveSongIds(context: PlayContext): List<Long> = when (context) {
        is PlayContext.All -> songDao.getAllSongsSortedBy(context.sortMode).map { it.id }
        is PlayContext.Artist -> songDao.getAllSongsByArtist(context.artist).map { it.id }
        is PlayContext.Folder -> songDao.getAllSongsByFolder(context.path).map { it.id }
        is PlayContext.Playlist -> playlistSongDao.getAllSongsByPlaylist(context.playlistId).map { it.id }
        is PlayContext.Search -> searchDao.getAllSongsByKeyWords(context.keyword, context.sortMode).first().map { it.id }
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
        val orders = FractionalIndexer.generateInitialList(if (anchorInList) allQueueIds.size else allQueueIds.size + 1)
        queueDao.updateShuffleOrder(anchorQueueId, orders[0])
        others.forEachIndexed { index, queueId -> queueDao.updateShuffleOrder(queueId, orders[index + 1]) }
    }
}
