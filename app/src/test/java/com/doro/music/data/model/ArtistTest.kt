package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class ArtistTest {

    @Test
    fun `Artist with default songCount`() {
        val artist = Artist(name = "Test Artist")

        assertEquals("Test Artist", artist.name)
        assertEquals(0, artist.songCount)
    }

    @Test
    fun `Artist with custom songCount`() {
        val artist = Artist(name = "Popular Artist", songCount = 42)

        assertEquals("Popular Artist", artist.name)
        assertEquals(42, artist.songCount)
    }

    @Test
    fun `Artist toString returns name`() {
        val artist = Artist(name = "My Artist")
        assertEquals("My Artist", artist.toString())
    }

    @Test
    fun `Artist equality works correctly`() {
        val artist1 = Artist(name = "Test", songCount = 10)
        val artist2 = Artist(name = "Test", songCount = 10)
        val artist3 = Artist(name = "Test", songCount = 20)

        assertEquals(artist1, artist2)
        assertNotEquals(artist1, artist3)
    }

    @Test
    fun `Artist copyWith creates modified copy`() {
        val original = Artist(name = "Original", songCount = 5)
        val modified = original.copy(songCount = 10)

        assertEquals("Original", modified.name)
        assertEquals(10, modified.songCount)
    }
}
