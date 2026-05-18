package com.doro.music.data.repo

import com.doro.music.data.datastore.SettingsDataStore
import com.doro.music.data.db.dao.FolderDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.FolderEntity
import com.doro.music.data.model.AppSettings
import com.doro.music.data.model.Song
import com.doro.music.player.util.MusicScanner
import com.doro.music.player.util.ScanResult
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MainRepoTest {

    private val mockSongDao = mockk<SongDao>(relaxed = true)
    private val mockFolderDao = mockk<FolderDao>(relaxed = true)
    private val mockSettingsDataStore = mockk<SettingsDataStore>()
    private val mockMusicScanner = mockk<MusicScanner>()

    private lateinit var repo: MainRepo

    @Before
    fun setup() {
        every { mockFolderDao.getAllFolders() } returns flowOf(emptyList())
        repo = MainRepo(mockSongDao, mockFolderDao, mockSettingsDataStore, mockMusicScanner)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `scan returns Success and syncs songs and folders`() = runTest {
        val songs = listOf(
            Song(id = 1L, title = "Song1", path = "/music/song1.mp3"),
            Song(id = 2L, title = "Song2", path = "/music/song2.mp3")
        )
        every { mockSettingsDataStore.settings } returns flowOf(AppSettings(minDurationFilter = 30))
        every { mockFolderDao.getAllFolders() } returns flowOf(emptyList())
        coEvery { mockMusicScanner.scan(30, emptyList()) } returns ScanResult.Success(songs)
        coEvery { mockSongDao.syncSongs(any()) } just Runs
        coEvery { mockFolderDao.syncFolders(any()) } just Runs

        val result = repo.scan()

        assertTrue(result is ScanResult.Success)
        assertEquals(2, (result as ScanResult.Success).songs.size)
        coVerify { mockSongDao.syncSongs(any()) }
        coVerify { mockFolderDao.syncFolders(any()) }
    }

    @Test
    fun `scan returns Error and does not sync`() = runTest {
        every { mockSettingsDataStore.settings } returns flowOf(AppSettings())
        every { mockFolderDao.getAllFolders() } returns flowOf(emptyList())
        // Default AppSettings has minDurationFilter=30, so scan is called with 30
        coEvery { mockMusicScanner.scan(30, emptyList()) } returns ScanResult.Error(Exception("fail"))

        val result = repo.scan()

        assertTrue(result is ScanResult.Error)
        coVerify(exactly = 0) { mockSongDao.syncSongs(any()) }
    }

    @Test
    fun `scan with default settings uses default minDuration`() = runTest {
        every { mockSettingsDataStore.settings } returns flowOf(AppSettings(minDurationFilter = 0))
        every { mockFolderDao.getAllFolders() } returns flowOf(emptyList())
        coEvery { mockMusicScanner.scan(0, emptyList()) } returns ScanResult.Success(emptyList())

        val result = repo.scan()

        assertTrue(result is ScanResult.Success)
        coVerify { mockMusicScanner.scan(0, emptyList()) }
    }

    @Test
    fun `scan excludes folders marked as excluded`() = runTest {
        val excludedFolders = listOf(
            FolderEntity(path = "/music/excluded", songCount = 1, excluded = true)
        )
        every { mockSettingsDataStore.settings } returns flowOf(AppSettings())
        every { mockFolderDao.getAllFolders() } returns flowOf(excludedFolders)
        // Default AppSettings has minDurationFilter=30
        coEvery { mockMusicScanner.scan(30, listOf("/music/excluded")) } returns ScanResult.Success(emptyList())

        val result = repo.scan()

        assertTrue(result is ScanResult.Success)
        coVerify { mockMusicScanner.scan(30, listOf("/music/excluded")) }
    }
}