package com.doro.music.data.repo

import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.SongEntity
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SongListRepoTest {

    private val mockSongDao = mockk<SongDao>()
    private val mockPlaylistSongDao = mockk<PlaylistSongDao>()
    private lateinit var repo: SongListRepo

    @Before
    fun setup() {
        repo = SongListRepo(mockSongDao, mockPlaylistSongDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAllSongsByArtist returns mapped songs`() = runTest {
        val entities = listOf(
            SongEntity(id = 1L, title = "Artist Song", path = "/a")
        )
        coEvery { mockSongDao.getAllSongsByArtist("TestArtist") } returns entities

        val result = repo.getAllSongsByArtist("TestArtist")
        assertEquals(1, result.size)
        assertEquals("Artist Song", result[0].title)
    }

    @Test
    fun `getAllSongsByPlaylist returns mapped songs`() = runTest {
        val entities = listOf(
            SongEntity(id = 2L, title = "Playlist Song", path = "/b")
        )
        coEvery { mockPlaylistSongDao.getAllSongsByPlaylist(5L) } returns entities

        val result = repo.getAllSongsByPlaylist(5L)
        assertEquals(1, result.size)
        assertEquals("Playlist Song", result[0].title)
    }

    @Test
    fun `removeSongFromPlaylist returns true when rows deleted`() = runTest {
        coEvery { mockPlaylistSongDao.removeSongFromPlaylist(1L, 100L) } returns 1

        val result = repo.removeSongFromPlaylist(1L, 100L)
        assertTrue(result)
    }

    @Test
    fun `removeSongFromPlaylist returns false when no rows deleted`() = runTest {
        coEvery { mockPlaylistSongDao.removeSongFromPlaylist(1L, 99L) } returns 0

        val result = repo.removeSongFromPlaylist(1L, 99L)
        assertFalse(result)
    }

    @Test
    fun `getSongCountByArtist delegates to DAO`() = runTest {
        every { mockSongDao.getSongCountByArtist("Artist") } returns kotlinx.coroutines.flow.flowOf(5)

        val count = repo.getSongCountByArtist("Artist").first()
        assertEquals(5, count)
    }

    @Test
    fun `getSongCountByPlaylist delegates to DAO`() = runTest {
        every { mockPlaylistSongDao.getSongCountByPlaylist(1L) } returns kotlinx.coroutines.flow.flowOf(3)

        val count = repo.getSongCountByPlaylist(1L).first()
        assertEquals(3, count)
    }
}