package com.doro.music.data.repo

import com.doro.music.data.datastore.PlayStateDataStore
import com.doro.music.data.db.dao.PlayQueueDao
import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.db.dao.SearchDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.SongEntity
import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.SortMode
import com.doro.music.player.model.PlayContext
import com.doro.music.player.model.QueueSong
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayQueueRepoTest {

    private val mockQueueDao = mockk<PlayQueueDao>(relaxed = true)
    private val mockSongDao = mockk<SongDao>()
    private val mockPlaylistSongDao = mockk<PlaylistSongDao>()
    private val mockSearchDao = mockk<SearchDao>()
    private val mockPlayStateDataStore = mockk<PlayStateDataStore>()

    private lateinit var repo: PlayQueueRepo

    @Before
    fun setup() {
        repo = PlayQueueRepo(
            queueDao = mockQueueDao,
            songDao = mockSongDao,
            playlistSongDao = mockPlaylistSongDao,
            searchDao = mockSearchDao,
            store = mockPlayStateDataStore
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== playNewQueue ====================

    @Test
    fun `playNewQueue clears queue and inserts new entities`() = runTest {
        val songIds = listOf(1L, 2L, 3L)
        val targetSongId = 2L

        coEvery { mockQueueDao.clearQueue() } just Runs
        coEvery { mockQueueDao.insertAll(any()) } just Runs
        coEvery { mockQueueDao.getQueueIdBySongId(targetSongId) } returns 10L
        coEvery { mockQueueDao.getAllQueueIdsSorted() } returns listOf(10L, 11L, 12L)
        coEvery { mockQueueDao.updateShuffleOrder(any(), any()) } just Runs

        val result = repo.playNewQueue(songIds, targetSongId, PlayMode.REPEAT)

        assertEquals(10L, result)
        coVerify { mockQueueDao.clearQueue() }
        coVerify { mockQueueDao.insertAll(any()) }
    }

    @Test
    fun `playNewQueue returns null when target song not found`() = runTest {
        val songIds = listOf(1L, 2L)
        val targetSongId = 99L

        coEvery { mockQueueDao.clearQueue() } just Runs
        coEvery { mockQueueDao.insertAll(any()) } just Runs
        coEvery { mockQueueDao.getQueueIdBySongId(targetSongId) } returns null

        val result = repo.playNewQueue(songIds, targetSongId, PlayMode.REPEAT)

        assertNull(result)
    }

    // ==================== insertNext ====================

    @Test
    fun `insertNext inserts songs after current in REPEAT mode`() = runTest {
        val currentQueueId = 5L
        val songIdsToInsert = listOf(100L, 101L)

        coEvery { mockQueueDao.getSortOrder(currentQueueId) } returns "a1"
        coEvery { mockQueueDao.getNextSortOrder("a1") } returns "a2"
        coEvery { mockPlayStateDataStore.playMode } returns flowOf(PlayMode.REPEAT)
        coEvery { mockQueueDao.insertAll(any()) } just Runs

        repo.insertNext(currentQueueId, songIdsToInsert)

        coVerify { mockQueueDao.insertAll(any()) }
    }

    @Test
    fun `insertNext returns early when sortOrder is null`() = runTest {
        val currentQueueId = 5L
        val songIdsToInsert = listOf(100L)

        coEvery { mockQueueDao.getSortOrder(currentQueueId) } returns null

        repo.insertNext(currentQueueId, songIdsToInsert)

        coVerify(exactly = 0) { mockQueueDao.insertAll(any()) }
    }

    @Test
    fun `insertNext in SHUFFLE mode also inserts shuffle orders`() = runTest {
        val currentQueueId = 5L
        val songIdsToInsert = listOf(100L)

        coEvery { mockQueueDao.getSortOrder(currentQueueId) } returns "a1"
        coEvery { mockQueueDao.getNextSortOrder("a1") } returns "a2"
        coEvery { mockPlayStateDataStore.playMode } returns flowOf(PlayMode.SHUFFLE)
        coEvery { mockQueueDao.getShuffleOrder(currentQueueId) } returns "b1"
        coEvery { mockQueueDao.getNextShuffleOrder("b1") } returns "b2"
        coEvery { mockQueueDao.insertAll(any()) } just Runs

        repo.insertNext(currentQueueId, songIdsToInsert)

        coVerify { mockQueueDao.insertAll(any()) }
    }

    @Test
    fun `insertNext in SHUFFLE mode returns early when shuffleOrder is null`() = runTest {
        val currentQueueId = 5L
        val songIdsToInsert = listOf(100L)

        coEvery { mockQueueDao.getSortOrder(currentQueueId) } returns "a1"
        coEvery { mockQueueDao.getNextSortOrder("a1") } returns "a2"
        coEvery { mockPlayStateDataStore.playMode } returns flowOf(PlayMode.SHUFFLE)
        coEvery { mockQueueDao.getShuffleOrder(currentQueueId) } returns null

        repo.insertNext(currentQueueId, songIdsToInsert)

        coVerify(exactly = 0) { mockQueueDao.insertAll(any()) }
    }

    // ==================== switchToShuffle ====================

    @Test
    fun `switchToShuffle updates shuffle orders for all queue items`() = runTest {
        val currentQueueId = 1L
        val allQueueIds = listOf(1L, 2L, 3L)

        coEvery { mockQueueDao.getAllQueueIdsSorted() } returns allQueueIds
        coEvery { mockQueueDao.updateShuffleOrder(any(), any()) } just Runs

        repo.switchToShuffle(currentQueueId)

        coVerify(exactly = 3) { mockQueueDao.updateShuffleOrder(any(), any()) }
    }

    // ==================== switchToSequential / switchToRepeatOne ====================

    @Test
    fun `switchToSequential does nothing`() = runTest {
        repo.switchToSequential()
        // No exception thrown, no-op
    }

    @Test
    fun `switchToRepeatOne does nothing`() = runTest {
        repo.switchToRepeatOne()
        // No exception thrown, no-op
    }

    // ==================== removeByQueueId ====================

    @Test
    fun `removeByQueueId delegates to DAO`() = runTest {
        val queueId = 5L
        coEvery { mockQueueDao.removeByQueueId(queueId) } just Runs

        repo.removeByQueueId(queueId)

        coVerify { mockQueueDao.removeByQueueId(queueId) }
    }

    // ==================== swapItems ====================

    @Test
    fun `swapItems in SHUFFLE mode swaps shuffle orders`() = runTest {
        val queueId1 = 1L
        val queueId2 = 2L

        coEvery { mockQueueDao.getShuffleOrder(queueId1) } returns "b1"
        coEvery { mockQueueDao.getShuffleOrder(queueId2) } returns "b2"
        coEvery { mockQueueDao.updateShuffleOrder(any(), any()) } just Runs

        repo.swapItems(queueId1, queueId2, PlayMode.SHUFFLE)

        coVerify { mockQueueDao.updateShuffleOrder(queueId1, "b2") }
        coVerify { mockQueueDao.updateShuffleOrder(queueId2, "b1") }
    }

    @Test
    fun `swapItems in REPEAT mode swaps sort orders`() = runTest {
        val queueId1 = 1L
        val queueId2 = 2L

        coEvery { mockQueueDao.getSortOrder(queueId1) } returns "a1"
        coEvery { mockQueueDao.getSortOrder(queueId2) } returns "a2"
        coEvery { mockQueueDao.updateSortOrder(any(), any()) } just Runs

        repo.swapItems(queueId1, queueId2, PlayMode.REPEAT)

        coVerify { mockQueueDao.updateSortOrder(queueId1, "a2") }
        coVerify { mockQueueDao.updateSortOrder(queueId2, "a1") }
    }

    @Test
    fun `swapItems returns early when order is null`() = runTest {
        coEvery { mockQueueDao.getSortOrder(1L) } returns null

        repo.swapItems(1L, 2L, PlayMode.REPEAT)

        coVerify(exactly = 0) { mockQueueDao.updateSortOrder(any(), any()) }
    }

    // ==================== appendToQueue ====================

    @Test
    fun `appendToQueue appends songs at end of queue`() = runTest {
        val songIds = listOf(10L, 20L)

        coEvery { mockQueueDao.getMaxSortOrder() } returns "a5"
        coEvery { mockQueueDao.insertAll(any()) } just Runs

        repo.appendToQueue(songIds)

        coVerify { mockQueueDao.insertAll(any()) }
    }

    @Test
    fun `appendToQueue uses a0 as default when no max sort order`() = runTest {
        val songIds = listOf(10L)

        coEvery { mockQueueDao.getMaxSortOrder() } returns null
        coEvery { mockQueueDao.insertAll(any()) } just Runs

        repo.appendToQueue(songIds)

        coVerify { mockQueueDao.insertAll(any()) }
    }

    // ==================== reassignShuffleOrdersWithSeed ====================

    @Test
    fun `reassignShuffleOrdersWithSeed delegates to assignShuffleOrders`() = runTest {
        val anchorQueueId = 1L
        val seed = 12345L

        coEvery { mockQueueDao.getAllQueueIdsSorted() } returns listOf(1L, 2L)
        coEvery { mockQueueDao.updateShuffleOrder(any(), any()) } just Runs

        repo.reassignShuffleOrdersWithSeed(anchorQueueId, seed)

        coVerify(exactly = 2) { mockQueueDao.updateShuffleOrder(any(), any()) }
    }

    // ==================== getQueueSize ====================

    @Test
    fun `getQueueSize delegates to DAO`() = runTest {
        coEvery { mockQueueDao.getQueueSize() } returns 5

        assertEquals(5, repo.getQueueSize())
    }

    // ==================== getSongIdByQueueId ====================

    @Test
    fun `getSongIdByQueueId delegates to DAO`() = runTest {
        coEvery { mockQueueDao.getSongIdByQueueId(10L) } returns 42L

        assertEquals(42L, repo.getSongIdByQueueId(10L))
    }

    @Test
    fun `getSongIdByQueueId returns null when not found`() = runTest {
        coEvery { mockQueueDao.getSongIdByQueueId(99L) } returns null

        assertNull(repo.getSongIdByQueueId(99L))
    }

    // ==================== getQueueSongById ====================

    @Test
    fun `getQueueSongById delegates to DAO`() = runTest {
        val queueSong = QueueSong(
            song = SongEntity(id = 1L, title = "Test", path = "/test"),
            queueId = 10L,
            sortOrder = "a0",
            shuffleOrder = "b0"
        )
        coEvery { mockQueueDao.getQueueSongById(10L) } returns queueSong

        val result = repo.getQueueSongById(10L)
        assertNotNull(result)
        assertEquals(10L, result!!.queueId)
    }

    // ==================== getOrder ====================

    @Test
    fun `getOrder returns shuffle order in SHUFFLE mode`() = runTest {
        coEvery { mockQueueDao.getShuffleOrder(1L) } returns "b1"

        assertEquals("b1", repo.getOrder(1L, PlayMode.SHUFFLE))
    }

    @Test
    fun `getOrder returns sort order in REPEAT mode`() = runTest {
        coEvery { mockQueueDao.getSortOrder(1L) } returns "a1"

        assertEquals("a1", repo.getOrder(1L, PlayMode.REPEAT))
    }

    @Test
    fun `getOrder returns sort order in REPEAT_ONE mode`() = runTest {
        coEvery { mockQueueDao.getSortOrder(1L) } returns "a1"

        assertEquals("a1", repo.getOrder(1L, PlayMode.REPEAT_ONE))
    }

    // ==================== getNextQueueId ====================

    @Test
    fun `getNextQueueId returns next queue id in REPEAT mode`() = runTest {
        coEvery { mockQueueDao.getNextSortOrder("a1") } returns "a2"
        coEvery { mockQueueDao.getQueueIdByExactSortOrder("a2") } returns 2L

        assertEquals(2L, repo.getNextQueueId("a1", PlayMode.REPEAT))
    }

    @Test
    fun `getNextQueueId returns next queue id in SHUFFLE mode`() = runTest {
        coEvery { mockQueueDao.getNextShuffleOrder("b1") } returns "b2"
        coEvery { mockQueueDao.getQueueIdByExactShuffleOrder("b2") } returns 3L

        assertEquals(3L, repo.getNextQueueId("b1", PlayMode.SHUFFLE))
    }

    @Test
    fun `getNextQueueId returns null when no next order`() = runTest {
        coEvery { mockQueueDao.getNextSortOrder("a5") } returns null

        assertNull(repo.getNextQueueId("a5", PlayMode.REPEAT))
    }

    // ==================== getPrevQueueId ====================

    @Test
    fun `getPrevQueueId returns previous queue id in REPEAT mode`() = runTest {
        coEvery { mockQueueDao.getPrevSortOrder("a2") } returns "a1"
        coEvery { mockQueueDao.getQueueIdByExactSortOrder("a1") } returns 1L

        assertEquals(1L, repo.getPrevQueueId("a2", PlayMode.REPEAT))
    }

    @Test
    fun `getPrevQueueId returns previous queue id in SHUFFLE mode`() = runTest {
        coEvery { mockQueueDao.getPrevShuffleOrder("b2") } returns "b1"
        coEvery { mockQueueDao.getQueueIdByExactShuffleOrder("b1") } returns 1L

        assertEquals(1L, repo.getPrevQueueId("b2", PlayMode.SHUFFLE))
    }

    @Test
    fun `getPrevQueueId returns null when no prev order`() = runTest {
        coEvery { mockQueueDao.getPrevSortOrder("a0") } returns null

        assertNull(repo.getPrevQueueId("a0", PlayMode.REPEAT))
    }

    // ==================== getFirstQueueId ====================

    @Test
    fun `getFirstQueueId returns first in REPEAT mode`() = runTest {
        coEvery { mockQueueDao.getMinSortOrder() } returns "a0"
        coEvery { mockQueueDao.getQueueIdByExactSortOrder("a0") } returns 1L

        assertEquals(1L, repo.getFirstQueueId(PlayMode.REPEAT))
    }

    @Test
    fun `getFirstQueueId returns first in SHUFFLE mode`() = runTest {
        coEvery { mockQueueDao.getMinShuffleOrder() } returns "b0"
        coEvery { mockQueueDao.getQueueIdByExactShuffleOrder("b0") } returns 1L

        assertEquals(1L, repo.getFirstQueueId(PlayMode.SHUFFLE))
    }

    @Test
    fun `getFirstQueueId returns null when no items`() = runTest {
        coEvery { mockQueueDao.getMinSortOrder() } returns null

        assertNull(repo.getFirstQueueId(PlayMode.REPEAT))
    }

    // ==================== getLastQueueId ====================

    @Test
    fun `getLastQueueId returns last in REPEAT mode`() = runTest {
        coEvery { mockQueueDao.getMaxSortOrder() } returns "a5"
        coEvery { mockQueueDao.getQueueIdByExactSortOrder("a5") } returns 5L

        assertEquals(5L, repo.getLastQueueId(PlayMode.REPEAT))
    }

    @Test
    fun `getLastQueueId returns last in SHUFFLE mode`() = runTest {
        coEvery { mockQueueDao.getMaxShuffleOrder() } returns "b5"
        coEvery { mockQueueDao.getQueueIdByExactShuffleOrder("b5") } returns 5L

        assertEquals(5L, repo.getLastQueueId(PlayMode.SHUFFLE))
    }

    @Test
    fun `getLastQueueId returns null when no items`() = runTest {
        coEvery { mockQueueDao.getMaxSortOrder() } returns null

        assertNull(repo.getLastQueueId(PlayMode.REPEAT))
    }

    // ==================== resolveSongIds ====================

    @Test
    fun `resolveSongIds with All context returns all song IDs`() = runTest {
        val entities = listOf(
            SongEntity(id = 1L, title = "Song1", path = "/a"),
            SongEntity(id = 2L, title = "Song2", path = "/b")
        )
        coEvery { mockSongDao.getAllSongsSortedBy(SortMode.TITLE) } returns entities

        val result = repo.resolveSongIds(PlayContext.All(SortMode.TITLE))
        assertEquals(listOf(1L, 2L), result)
    }

    @Test
    fun `resolveSongIds with Artist context returns artist song IDs`() = runTest {
        val entities = listOf(SongEntity(id = 10L, title = "A", path = "/a"))
        coEvery { mockSongDao.getAllSongsByArtist("TestArtist") } returns entities

        val result = repo.resolveSongIds(PlayContext.Artist("TestArtist", SortMode.TITLE))
        assertEquals(listOf(10L), result)
    }

    @Test
    fun `resolveSongIds with Folder context returns folder song IDs`() = runTest {
        val entities = listOf(SongEntity(id = 20L, title = "F", path = "/folder/a"))
        coEvery { mockSongDao.getAllSongsByFolder("/folder") } returns entities

        val result = repo.resolveSongIds(PlayContext.Folder("/folder", SortMode.TITLE))
        assertEquals(listOf(20L), result)
    }

    @Test
    fun `resolveSongIds with Playlist context returns playlist song IDs`() = runTest {
        val entities = listOf(SongEntity(id = 30L, title = "P", path = "/p"))
        coEvery { mockPlaylistSongDao.getAllSongsByPlaylist(5L) } returns entities

        val result = repo.resolveSongIds(PlayContext.Playlist(5L, SortMode.TITLE))
        assertEquals(listOf(30L), result)
    }

    @Test
    fun `resolveSongIds with Search context returns search song IDs`() = runTest {
        val entities = listOf(SongEntity(id = 40L, title = "S", path = "/s"))
        coEvery { mockSearchDao.getAllSongsByKeyWords("test", SortMode.TITLE) } returns flowOf(entities)

        val result = repo.resolveSongIds(PlayContext.Search("test", SortMode.TITLE))
        assertEquals(listOf(40L), result)
    }
}
