package com.doro.music.data.repo

import com.doro.music.data.db.dao.LyricsDao
import com.doro.music.data.db.entities.LyricsEntity
import com.doro.music.data.model.LyricsSource
import com.doro.music.data.model.Song
import com.doro.music.player.lyrics.LrcFileLoader
import com.doro.music.player.lyrics.LrcParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LyricsRepoTest {

    private val mockLrcFileLoader = mockk<LrcFileLoader>()
    private val mockLyricsDao = mockk<LyricsDao>(relaxed = true)
    private lateinit var repo: LyricsRepo

    private val testSong = Song(id = 1L, title = "Test Song", path = "/music/test.mp3")

    @Before
    fun setup() {
        repo = LyricsRepo(mockLrcFileLoader, mockLyricsDao)
    }

    @Test
    fun `getLyrics returns cached lyrics when available`() = runTest {
        val cachedEntity = LyricsEntity(
            songId = 1L,
            lrcContent = "[00:05.00]Cached line",
            source = LyricsSource.CACHE.name,
            offset = 100L
        )
        coEvery { mockLyricsDao.getBySongId(1L) } returns cachedEntity

        val result = repo.getLyrics(testSong)

        assertEquals(LyricsSource.CACHE, result!!.source)
        assertEquals(1L, result.songId)
        assertEquals(100L, result.offset)
        assertEquals(1, result.lines.size)
        assertEquals("Cached line", result.lines[0].text)
    }

    @Test
    fun `getLyrics returns local LRC when no cache`() = runTest {
        coEvery { mockLyricsDao.getBySongId(1L) } returns null

        val parsedLyrics = LrcParser.parse("[00:10.00]Local line")!!.copy(
            songId = 1L,
            source = LyricsSource.LOCAL_LRC
        )
        coEvery { mockLrcFileLoader.loadForSong(testSong) } returns parsedLyrics

        val result = repo.getLyrics(testSong)

        assertEquals(LyricsSource.LOCAL_LRC, result!!.source)
        assertEquals(1L, result.songId)
        assertEquals("Local line", result.lines[0].text)

        // Verify it was cached
        coVerify { mockLyricsDao.insert(any()) }
    }

    @Test
    fun `getLyrics returns null when no cache and no local file`() = runTest {
        coEvery { mockLyricsDao.getBySongId(1L) } returns null
        coEvery { mockLrcFileLoader.loadForSong(testSong) } returns null

        val result = repo.getLyrics(testSong)

        assertNull(result)
    }

    @Test
    fun `getLyrics returns null when cached LRC content is invalid`() = runTest {
        val cachedEntity = LyricsEntity(
            songId = 1L,
            lrcContent = "not valid lrc content without timestamps",
            source = LyricsSource.CACHE.name
        )
        coEvery { mockLyricsDao.getBySongId(1L) } returns cachedEntity

        val result = repo.getLyrics(testSong)

        assertNull(result)
    }

    @Test
    fun `getLyrics parses cached lrc content`() = runTest {
        val cachedEntity = LyricsEntity(
            songId = 1L,
            lrcContent = "[00:05.00]Cached line",
            source = LyricsSource.CACHE.name,
            offset = 100L
        )
        coEvery { mockLyricsDao.getBySongId(1L) } returns cachedEntity

        val result = repo.getLyrics(testSong)

        assertEquals(LyricsSource.CACHE, result!!.source)
        assertEquals(1L, result.songId)
        assertEquals(100L, result.offset)
        assertEquals("Cached line", result.lines[0].text)
    }
}
