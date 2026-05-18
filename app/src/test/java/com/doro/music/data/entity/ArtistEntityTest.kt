package com.doro.music.data.entity

import com.doro.music.data.db.entities.ArtistEntity
import com.doro.music.data.db.entities.toArtist
import com.doro.music.data.db.entities.toArtists
import org.junit.Assert.*
import org.junit.Test

class ArtistEntityTest {

    @Test
    fun `ArtistEntity toArtist conversion is correct`() {
        val entity = ArtistEntity(
            name = "Test Artist",
            songCount = 25
        )

        val artist = entity.toArtist()

        assertEquals("Test Artist", artist.name)
        assertEquals(25, artist.songCount)
    }

    @Test
    fun `ArtistEntity toArtist with zero songCount`() {
        val entity = ArtistEntity(
            name = "Unknown Artist",
            songCount = 0
        )

        val artist = entity.toArtist()

        assertEquals("Unknown Artist", artist.name)
        assertEquals(0, artist.songCount)
    }

    @Test
    fun `ListArtistEntity toArtists conversion is correct`() {
        val entities = listOf(
            ArtistEntity(name = "Artist A", songCount = 10),
            ArtistEntity(name = "Artist B", songCount = 20),
            ArtistEntity(name = "Artist C", songCount = 30)
        )

        val artists = entities.toArtists()

        assertEquals(3, artists.size)
        assertEquals("Artist A", artists[0].name)
        assertEquals(10, artists[0].songCount)
        assertEquals("Artist B", artists[1].name)
        assertEquals(20, artists[1].songCount)
        assertEquals("Artist C", artists[2].name)
        assertEquals(30, artists[2].songCount)
    }
}
