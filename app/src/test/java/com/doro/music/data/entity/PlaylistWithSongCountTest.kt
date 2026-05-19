package com.doro.music.data.entity

import android.os.SystemClock
import com.doro.music.data.db.entities.PlaylistEntity
import com.doro.music.data.db.entities.PlaylistWithSongCount
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class PlaylistWithSongCountTest {

    @Before
    fun setup() {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } returns 1000L
    }

    @After
    fun tearDown() {
        unmockkStatic(SystemClock::class)
    }

    @Test
    fun `PlaylistWithSongCount holds embedded playlist and songCount`() {
        val playlist = PlaylistEntity(id = 1L, name = "My Playlist")
        val withCount = PlaylistWithSongCount(playlist = playlist, songCount = 5)

        assertEquals(playlist, withCount.playlist)
        assertEquals(5, withCount.songCount)
    }

    @Test
    fun `PlaylistWithSongCount with zero songCount`() {
        val playlist = PlaylistEntity(id = 2L, name = "Empty Playlist")
        val withCount = PlaylistWithSongCount(playlist = playlist, songCount = 0)

        assertEquals(0, withCount.songCount)
        assertEquals("Empty Playlist", withCount.playlist.name)
    }

    @Test
    fun `PlaylistWithSongCount equality based on content`() {
        val playlist1 = PlaylistEntity(id = 1L, name = "Playlist")
        val playlist2 = PlaylistEntity(id = 1L, name = "Playlist")
        val count1 = PlaylistWithSongCount(playlist = playlist1, songCount = 3)
        val count2 = PlaylistWithSongCount(playlist = playlist2, songCount = 3)

        assertEquals(count1, count2)
        assertEquals(count1.hashCode(), count2.hashCode())
    }

    @Test
    fun `PlaylistWithSongCount inequality with different songCount`() {
        val playlist = PlaylistEntity(id = 1L, name = "Playlist")
        val count1 = PlaylistWithSongCount(playlist = playlist, songCount = 3)
        val count2 = PlaylistWithSongCount(playlist = playlist, songCount = 5)

        assertNotEquals(count1, count2)
    }

    @Test
    fun `PlaylistWithSongCount copy preserves data`() {
        val playlist = PlaylistEntity(id = 1L, name = "Original")
        val original = PlaylistWithSongCount(playlist = playlist, songCount = 10)
        val copied = original.copy(songCount = 20)

        assertEquals(1L, copied.playlist.id)
        assertEquals("Original", copied.playlist.name)
        assertEquals(20, copied.songCount)
    }
}
