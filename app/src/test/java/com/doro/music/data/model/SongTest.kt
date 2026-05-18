package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class SongTest {

    @Test
    fun `Song default values are correct`() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            path = "/music/test.mp3"
        )

        assertEquals(1L, song.id)
        assertEquals("Test Song", song.title)
        assertEquals("/music/test.mp3", song.path)
        assertNull(song.uri)
        assertNull(song.artist)
        assertNull(song.album)
        assertNull(song.albumArt)
        assertNull(song.year)
        assertEquals(0L, song.duration)
        assertNull(song.mimeType)
        assertNull(song.genre)
        assertTrue(song.dateAdded > 0)
        assertEquals(0L, song.size)
    }

    @Test
    fun `Song with all fields populated correctly`() {
        val dateAdded = System.currentTimeMillis()
        val song = Song(
            id = 100L,
            uri = "content://media/audio/100",
            title = "Complete Song",
            artist = "Test Artist",
            album = "Test Album",
            albumArt = "content://artwork/100",
            year = 2024,
            duration = 180000L,
            mimeType = "audio/mpeg",
            path = "/storage/music/complete.mp3",
            genre = "Rock",
            dateAdded = dateAdded,
            size = 5000000L
        )

        assertEquals(100L, song.id)
        assertEquals("content://media/audio/100", song.uri)
        assertEquals("Complete Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals("Test Album", song.album)
        assertEquals("content://artwork/100", song.albumArt)
        assertEquals(2024, song.year)
        assertEquals(180000L, song.duration)
        assertEquals("audio/mpeg", song.mimeType)
        assertEquals("/storage/music/complete.mp3", song.path)
        assertEquals("Rock", song.genre)
        assertEquals(dateAdded, song.dateAdded)
        assertEquals(5000000L, song.size)
    }

    @Test
    fun `Song equality works correctly`() {
        val song1 = Song(id = 1L, title = "Song", path = "/path1")
        val song2 = Song(id = 1L, title = "Song", path = "/path1")
        val song3 = Song(id = 2L, title = "Song", path = "/path1")

        assertEquals(song1, song2)
        assertNotEquals(song1, song3)
    }

    @Test
    fun `Song copyWith creates modified copy`() {
        val original = Song(
            id = 1L,
            title = "Original",
            artist = "Original Artist",
            path = "/original"
        )
        val modified = original.copy(title = "Modified", artist = "Modified Artist")

        assertEquals(1L, modified.id)
        assertEquals("Modified", modified.title)
        assertEquals("Modified Artist", modified.artist)
        assertEquals("/original", modified.path)
    }
}
