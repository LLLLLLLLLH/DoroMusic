package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class PlaylistTest {

    @Test
    fun `Playlist with default songCount`() {
        val playlist = Playlist(
            id = 1L,
            name = "My Playlist"
        )

        assertEquals(1L, playlist.id)
        assertEquals("My Playlist", playlist.name)
        assertEquals(0, playlist.songCount)
    }

    @Test
    fun `Playlist with custom songCount`() {
        val playlist = Playlist(
            id = 2L,
            name = "Pop Songs",
            songCount = 15
        )

        assertEquals(2L, playlist.id)
        assertEquals("Pop Songs", playlist.name)
        assertEquals(15, playlist.songCount)
    }

    @Test
    fun `Playlist equality works correctly`() {
        val playlist1 = Playlist(id = 1L, name = "Test")
        val playlist2 = Playlist(id = 1L, name = "Test")
        val playlist3 = Playlist(id = 2L, name = "Test")

        assertEquals(playlist1, playlist2)
        assertNotEquals(playlist1, playlist3)
    }

    @Test
    fun `Playlist copyWith creates modified copy`() {
        val original = Playlist(id = 1L, name = "Original", songCount = 5)
        val modified = original.copy(name = "Modified", songCount = 10)

        assertEquals(1L, modified.id)
        assertEquals("Modified", modified.name)
        assertEquals(10, modified.songCount)
    }
}
