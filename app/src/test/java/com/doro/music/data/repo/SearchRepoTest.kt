package com.doro.music.data.repo

import com.doro.music.data.db.dao.SearchDao
import com.doro.music.data.db.entities.SongEntity
import com.doro.music.data.model.SortMode
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchRepoTest {

    private val mockSearchDao = mockk<SearchDao>()
    private lateinit var repo: SearchRepo

    @Before
    fun setup() {
        repo = SearchRepo(mockSearchDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getSongCountByKeyWords delegates to DAO`() = runTest {
        every { mockSearchDao.getSongCountByKeyWords("test") } returns flowOf(3)

        val count = repo.getSongCountByKeyWords("test").first()
        assertEquals(3, count)
    }

    @Test
    fun `getAllSongsByKeyWords returns mapped songs`() = runTest {
        val entities = listOf(
            SongEntity(id = 1L, title = "Found Song", path = "/a")
        )
        every { mockSearchDao.getAllSongsByKeyWords("test", SortMode.TITLE) } returns flowOf(entities)

        val result = repo.getAllSongsByKeyWords("test", SortMode.TITLE).first()
        assertEquals(1, result.size)
        assertEquals("Found Song", result[0].title)
    }

    @Test
    fun `getAllSongsByKeyWords returns empty list when no matches`() = runTest {
        every { mockSearchDao.getAllSongsByKeyWords("xyz", SortMode.TITLE) } returns flowOf(emptyList())

        val result = repo.getAllSongsByKeyWords("xyz", SortMode.TITLE).first()
        assertTrue(result.isEmpty())
    }
}