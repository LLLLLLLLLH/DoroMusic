package com.doro.music.data.repo

import android.os.SystemClock
import com.doro.music.data.db.dao.PlaylistDao
import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PlaylistRepoTest {

    private val mockPlaylistDao = mockk<PlaylistDao>()
    private val mockPlaylistSongDao = mockk<PlaylistSongDao>()

    private lateinit var repo: PlaylistRepo

    @Before
    fun setup() {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } returns 1000L

        repo = PlaylistRepo(mockPlaylistDao, mockPlaylistSongDao)
    }

    @After
    fun tearDown() {
        unmockkStatic(SystemClock::class)
        unmockkAll()
    }

    @Test
    fun `createPlaylist returns true when rowId is valid`() = runTest {
        coEvery { mockPlaylistDao.createPlaylist(any()) } returns 1L

        val result = repo.createPlaylist("My Playlist")
        assertTrue(result)
    }

    @Test
    fun `createPlaylist returns false when rowId is -1`() = runTest {
        coEvery { mockPlaylistDao.createPlaylist(any()) } returns -1L

        val result = repo.createPlaylist("Duplicate")
        assertFalse(result)
    }

    @Test
    fun `deletePlaylist returns true when rows deleted`() = runTest {
        coEvery { mockPlaylistDao.deletePlaylist(1L) } returns 1

        val result = repo.deletePlaylist(1L)
        assertTrue(result)
    }

    @Test
    fun `deletePlaylist returns false when no rows deleted`() = runTest {
        coEvery { mockPlaylistDao.deletePlaylist(99L) } returns 0

        val result = repo.deletePlaylist(99L)
        assertFalse(result)
    }

    @Test
    fun `addSongToPlaylist returns Success when songs added`() = runTest {
        val playlists = listOf(Playlist(id = 1L, name = "P1"))
        val songId = 100L

        coEvery { mockPlaylistSongDao.addSongToPlaylist(any()) } returns listOf(1L)

        val result = repo.addSongToPlaylist(playlists, songId)
        assertEquals(AddSongResult.Success, result)
    }

    @Test
    fun `addSongToPlaylist returns AlreadyExists when all songs exist`() = runTest {
        val playlists = listOf(Playlist(id = 1L, name = "P1"))
        val songId = 100L

        coEvery { mockPlaylistSongDao.addSongToPlaylist(any()) } returns listOf(-1L)

        val result = repo.addSongToPlaylist(playlists, songId)
        assertEquals(AddSongResult.AlreadyExists, result)
    }

    @Test
    fun `addSongToPlaylist returns Success when some added and some existed`() = runTest {
        val playlists = listOf(Playlist(id = 1L, name = "P1"), Playlist(id = 2L, name = "P2"))
        val songId = 100L

        coEvery { mockPlaylistSongDao.addSongToPlaylist(any()) } returns listOf(1L, -1L)

        val result = repo.addSongToPlaylist(playlists, songId)
        assertEquals(AddSongResult.Success, result)
    }

    @Test
    fun `isPlaylistNameExists returns true when name exists`() = runTest {
        every { mockPlaylistDao.isPlaylistNameExists("Existing") } returns true

        assertTrue(repo.isPlaylistNameExists("Existing"))
    }

    @Test
    fun `isPlaylistNameExists returns false when name does not exist`() = runTest {
        every { mockPlaylistDao.isPlaylistNameExists("New") } returns false

        assertFalse(repo.isPlaylistNameExists("New"))
    }
}