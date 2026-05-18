package com.doro.music.data.repo

import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.SongEntity
import com.doro.music.data.model.SortMode
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SongRepoTest {

    private val mockSongDao = mockk<SongDao>()
    private lateinit var repo: SongRepo

    @Before
    fun setup() {
        repo = SongRepo(mockSongDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getSongCount delegates to DAO`() = runTest {
        // getSongCount returns a Flow, we just verify it delegates
        every { mockSongDao.getSongCount() } returns kotlinx.coroutines.flow.flowOf(10)
        val count = repo.getSongCount().first()
        assertEquals(10, count)
    }

    @Test
    fun `getAllSongs returns mapped songs`() = runTest {
        val entities = listOf(
            SongEntity(id = 1L, title = "Song1", path = "/a"),
            SongEntity(id = 2L, title = "Song2", path = "/b")
        )
        coEvery { mockSongDao.getAllSongsSortedBy(SortMode.TITLE) } returns entities

        val result = repo.getAllSongs(SortMode.TITLE)
        assertEquals(2, result.size)
        assertEquals("Song1", result[0].title)
        assertEquals("Song2", result[1].title)
    }

    @Test
    fun `getSongById returns mapped song when found`() = runTest {
        val entity = SongEntity(id = 1L, title = "Test Song", path = "/test")
        coEvery { mockSongDao.getSongById(1L) } returns entity

        val result = repo.getSongById(1L)
        assertNotNull(result)
        assertEquals("Test Song", result!!.title)
    }

    @Test
    fun `getSongById returns null when not found`() = runTest {
        coEvery { mockSongDao.getSongById(99L) } returns null

        val result = repo.getSongById(99L)
        assertNull(result)
    }
}