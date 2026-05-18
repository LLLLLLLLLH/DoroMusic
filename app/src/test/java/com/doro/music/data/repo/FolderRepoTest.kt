package com.doro.music.data.repo

import com.doro.music.data.db.dao.FolderDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.FolderEntity
import com.doro.music.data.db.entities.SongEntity
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FolderRepoTest {

    private val mockSongDao = mockk<SongDao>()
    private val mockFolderDao = mockk<FolderDao>()
    private lateinit var repo: FolderRepo

    @Before
    fun setup() {
        // FolderRepo reads folderDao.getAllFolders() in constructor, must mock before construction
        every { mockFolderDao.getAllFolders() } returns flowOf(emptyList())
        repo = FolderRepo(mockSongDao, mockFolderDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `folders maps FolderEntities to Folder models`() = runTest {
        // Need to re-create repo with different mock data
        val entities = listOf(
            FolderEntity(path = "/music/rock", songCount = 5, excluded = false),
            FolderEntity(path = "/music/pop", songCount = 3, excluded = true)
        )
        every { mockFolderDao.getAllFolders() } returns flowOf(entities)
        val testRepo = FolderRepo(mockSongDao, mockFolderDao)

        val result = testRepo.folders.first()
        assertEquals(2, result.size)
        assertEquals("/music/rock", result[0].path)
        assertEquals(5, result[0].songCount)
    }

    @Test
    fun `getAllSongsByFolder returns mapped songs`() = runTest {
        val entities = listOf(
            SongEntity(id = 1L, title = "Rock Song", path = "/music/rock/song.mp3")
        )
        coEvery { mockSongDao.getAllSongsByFolder("/music/rock") } returns entities

        val result = repo.getAllSongsByFolder("/music/rock")
        assertEquals(1, result.size)
        assertEquals("Rock Song", result[0].title)
    }

    @Test
    fun `getAllSongsByFolder returns empty list when no songs`() = runTest {
        coEvery { mockSongDao.getAllSongsByFolder("/empty") } returns emptyList()

        val result = repo.getAllSongsByFolder("/empty")
        assertTrue(result.isEmpty())
    }
}