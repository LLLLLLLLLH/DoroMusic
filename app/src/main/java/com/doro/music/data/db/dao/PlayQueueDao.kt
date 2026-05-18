package com.doro.music.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.doro.music.data.db.entities.PlayerQueueEntity
import com.doro.music.player.model.QueueSong

@Dao
interface PlayQueueDao {

    @Query("""
        SELECT s.*, pq.queue_id as queueId, pq.sort_order as sortOrder, pq.shuffle_order as shuffleOrder
        FROM songs s
        INNER JOIN play_queue pq ON s.id = pq.song_id
        ORDER BY pq.sort_order ASC
    """)
    fun getPlayQueueSorted(): PagingSource<Int, QueueSong>

    @Query("""
        SELECT s.*, pq.queue_id as queueId, pq.sort_order as sortOrder, pq.shuffle_order as shuffleOrder
        FROM songs s
        INNER JOIN play_queue pq ON s.id = pq.song_id
        ORDER BY pq.shuffle_order ASC
    """)
    fun getPlayQueueShuffled(): PagingSource<Int, QueueSong>

    @Query("""
        SELECT s.*, pq.queue_id as queueId, pq.sort_order as sortOrder, pq.shuffle_order as shuffleOrder
        FROM songs s
        INNER JOIN play_queue pq ON s.id = pq.song_id
        WHERE pq.sort_order BETWEEN :fromOrder AND :toOrder
        ORDER BY pq.sort_order ASC
    """)
    suspend fun getWindowSongsSorted(fromOrder: String, toOrder: String): List<QueueSong>

    @Query("""
        SELECT s.*, pq.queue_id as queueId, pq.sort_order as sortOrder, pq.shuffle_order as shuffleOrder
        FROM songs s
        INNER JOIN play_queue pq ON s.id = pq.song_id
        WHERE pq.shuffle_order BETWEEN :fromOrder AND :toOrder
        ORDER BY pq.shuffle_order ASC
    """)
    suspend fun getWindowSongsShuffled(fromOrder: String, toOrder: String): List<QueueSong>

    @Query("SELECT sort_order FROM play_queue WHERE queue_id = :queueId")
    suspend fun getSortOrder(queueId: Long): String?

    @Query("SELECT shuffle_order FROM play_queue WHERE queue_id = :queueId")
    suspend fun getShuffleOrder(queueId: Long): String?

    @Query("SELECT queue_id FROM play_queue ORDER BY sort_order ASC")
    suspend fun getAllQueueIdsSorted(): List<Long>

    @Query("SELECT song_id FROM play_queue WHERE queue_id = :queueId")
    suspend fun getSongIdByQueueId(queueId: Long): Long?

    @Query("""
        SELECT s.*, pq.queue_id as queueId, pq.sort_order as sortOrder, pq.shuffle_order as shuffleOrder
        FROM songs s
        INNER JOIN play_queue pq ON s.id = pq.song_id
        WHERE pq.queue_id = :queueId
        LIMIT 1
    """)
    suspend fun getQueueSongById(queueId: Long): QueueSong?

    @Query("SELECT queue_id FROM play_queue WHERE song_id = :songId LIMIT 1")
    suspend fun getQueueIdBySongId(songId: Long): Long?

    @Query("SELECT COUNT(*) FROM play_queue")
    suspend fun getQueueSize(): Int

    @Query("""
        SELECT queue_id FROM play_queue
        WHERE sort_order >= :targetOrder
        ORDER BY sort_order ASC
        LIMIT 1
    """)
    suspend fun getQueueIdBySortOrder(targetOrder: String): Long?

    @Query("""
        SELECT queue_id FROM play_queue
        WHERE shuffle_order >= :targetOrder
        ORDER BY shuffle_order ASC
        LIMIT 1
    """)
    suspend fun getQueueIdByShuffleOrder(targetOrder: String): Long?

    @Query("""
        SELECT queue_id FROM play_queue
        WHERE sort_order = :targetOrder
        LIMIT 1
    """)
    suspend fun getQueueIdByExactSortOrder(targetOrder: String): Long?

    @Query("""
        SELECT queue_id FROM play_queue
        WHERE shuffle_order = :targetOrder
        LIMIT 1
    """)
    suspend fun getQueueIdByExactShuffleOrder(targetOrder: String): Long?

    @Query("SELECT sort_order FROM play_queue ORDER BY sort_order ASC LIMIT 1")
    suspend fun getMinSortOrder(): String?

    @Query("SELECT sort_order FROM play_queue ORDER BY sort_order DESC LIMIT 1")
    suspend fun getMaxSortOrder(): String?

    @Query("SELECT shuffle_order FROM play_queue ORDER BY shuffle_order ASC LIMIT 1")
    suspend fun getMinShuffleOrder(): String?

    @Query("SELECT shuffle_order FROM play_queue ORDER BY shuffle_order DESC LIMIT 1")
    suspend fun getMaxShuffleOrder(): String?

    @Query("""
        SELECT sort_order FROM play_queue
        WHERE sort_order > :currentOrder
        ORDER BY sort_order ASC
        LIMIT 1
    """)
    suspend fun getNextSortOrder(currentOrder: String): String?

    @Query("""
        SELECT sort_order FROM play_queue
        WHERE sort_order < :currentOrder
        ORDER BY sort_order DESC
        LIMIT 1
    """)
    suspend fun getPrevSortOrder(currentOrder: String): String?

    @Query("""
        SELECT shuffle_order FROM play_queue
        WHERE shuffle_order > :currentOrder
        ORDER BY shuffle_order ASC
        LIMIT 1
    """)
    suspend fun getNextShuffleOrder(currentOrder: String): String?

    @Query("""
        SELECT shuffle_order FROM play_queue
        WHERE shuffle_order < :currentOrder
        ORDER BY shuffle_order DESC
        LIMIT 1
    """)
    suspend fun getPrevShuffleOrder(currentOrder: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PlayerQueueEntity>)

    @Query("DELETE FROM play_queue")
    suspend fun clearQueue()

    @Query("DELETE FROM play_queue WHERE queue_id = :queueId")
    suspend fun removeByQueueId(queueId: Long)

    @Query("UPDATE play_queue SET sort_order = :newOrder WHERE queue_id = :queueId")
    suspend fun updateSortOrder(queueId: Long, newOrder: String)

    @Query("UPDATE play_queue SET shuffle_order = :newOrder WHERE queue_id = :queueId")
    suspend fun updateShuffleOrder(queueId: Long, newOrder: String)

    @Transaction
    suspend fun reassignShuffleOrders(assignments: List<Pair<Long, String>>) {
        for ((queueId, order) in assignments) {
            updateShuffleOrder(queueId, order)
        }
    }
}