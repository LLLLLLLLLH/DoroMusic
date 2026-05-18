package com.doro.music.data.entity

import com.doro.music.data.db.entities.SongEntity
import com.doro.music.data.db.entities.toSong
import com.doro.music.data.db.entities.toEntity
import com.doro.music.data.db.entities.toSongs
import com.doro.music.data.db.entities.toSongEntities
import com.doro.music.data.model.Song
import org.junit.Assert.*
import org.junit.Test

class SongEntityTest {

    @Test
    fun `SongEntity toSong conversion is correct`() {
        val entity = SongEntity(
            id = 1L,
            uri = "content://media/audio/1",
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            albumArt = "content://artwork/1",
            genre = "Rock",
            year = 2024,
            duration = 180000L,
            mimeType = "audio/mpeg",
            path = "/storage/music/test.mp3",
            dateAdded = 1700000000L,
            size = 5000000L
        )

        val song = entity.toSong()

        assertEquals(1L, song.id)
        assertEquals("content://media/audio/1", song.uri)
        assertEquals("Test Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals("Test Album", song.album)
        assertEquals("content://artwork/1", song.albumArt)
        assertEquals("Rock", song.genre)
        assertEquals(2024, song.year)
        assertEquals(180000L, song.duration)
        assertEquals("audio/mpeg", song.mimeType)
        assertEquals("/storage/music/test.mp3", song.path)
        assertEquals(1700000000L, song.dateAdded)
        assertEquals(5000000L, song.size)
    }

    @Test
    fun `Song toEntity conversion is correct`() {
        val song = Song(
            id = 2L,
            uri = "content://media/audio/2",
            title = "My Song",
            artist = "My Artist",
            album = "My Album",
            albumArt = "content://artwork/2",
            genre = "Pop",
            year = 2025,
            duration = 200000L,
            mimeType = "audio/mp4",
            path = "/storage/music/mysong.mp4",
            dateAdded = 1700000001L,
            size = 6000000L
        )

        val entity = song.toEntity()

        assertEquals(2L, entity.id)
        assertEquals("content://media/audio/2", entity.uri)
        assertEquals("My Song", entity.title)
        assertEquals("My Artist", entity.artist)
        assertEquals("My Album", entity.album)
        assertEquals("content://artwork/2", entity.albumArt)
        assertEquals("Pop", entity.genre)
        assertEquals(2025, entity.year)
        assertEquals(200000L, entity.duration)
        assertEquals("audio/mp4", entity.mimeType)
        assertEquals("/storage/music/mysong.mp4", entity.path)
        assertEquals(1700000001L, entity.dateAdded)
        assertEquals(6000000L, entity.size)
    }

    @Test
    fun `SongEntity toSong roundtrip preserves data`() {
        val originalEntity = SongEntity(
            id = 5L,
            title = "Roundtrip Song",
            artist = "Roundtrip Artist",
            path = "/roundtrip/song.mp3"
        )

        val song = originalEntity.toSong()
        val recoveredEntity = song.toEntity()

        assertEquals(originalEntity.id, recoveredEntity.id)
        assertEquals(originalEntity.title, recoveredEntity.title)
        assertEquals(originalEntity.artist, recoveredEntity.artist)
        assertEquals(originalEntity.path, recoveredEntity.path)
    }

    @Test
    fun `ListSongEntity toSongs conversion is correct`() {
        val entities = listOf(
            SongEntity(id = 1L, title = "Song 1", path = "/path1"),
            SongEntity(id = 2L, title = "Song 2", path = "/path2")
        )

        val songs = entities.toSongs()

        assertEquals(2, songs.size)
        assertEquals("Song 1", songs[0].title)
        assertEquals("Song 2", songs[1].title)
    }

    @Test
    fun `ListSong toSongEntities conversion is correct`() {
        val songs = listOf(
            Song(id = 1L, title = "Song A", path = "/pathA"),
            Song(id = 2L, title = "Song B", path = "/pathB")
        )

        val entities = songs.toSongEntities()

        assertEquals(2, entities.size)
        assertEquals("Song A", entities[0].title)
        assertEquals("Song B", entities[1].title)
    }
}
