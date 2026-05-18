package com.doro.music.data.entity

import com.doro.music.data.db.entities.FolderEntity
import com.doro.music.data.db.entities.toFolder
import com.doro.music.data.db.entities.toFolderEntities
import com.doro.music.data.db.entities.toFolders
import com.doro.music.data.model.Song
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FolderEntityTest {

    @Test
    fun `FolderEntity toFolder conversion is correct`() {
        val entity = FolderEntity(
            path = "/storage/music/Rock",
            songCount = 15,
            excluded = false
        )

        val folder = entity.toFolder()

        assertEquals("/storage/music/Rock", folder.path)
        assertEquals(15, folder.songCount)
        assertFalse(folder.excluded)
    }

    @Test
    fun `FolderEntity toFolder with excluded true`() {
        val entity = FolderEntity(
            path = "/storage/music/Excluded",
            songCount = 5,
            excluded = true
        )

        val folder = entity.toFolder()

        assertEquals("/storage/music/Excluded", folder.path)
        assertEquals(5, folder.songCount)
        assertTrue(folder.excluded)
    }

    @Test
    fun `ListFolderEntity toFolders conversion is correct`() {
        val entities = listOf(
            FolderEntity(path = "/music/rock", songCount = 10),
            FolderEntity(path = "/music/pop", songCount = 20)
        )

        val folders = entities.toFolders()

        assertEquals(2, folders.size)
        assertEquals("/music/rock", folders[0].path)
        assertEquals("/music/pop", folders[1].path)
    }

    @Test
    fun `ListSong toFolderEntities groups by parent directory`() {
        val songs = listOf(
            Song(id = 1L, title = "Song 1", path = "/music/rock/song1.mp3"),
            Song(id = 2L, title = "Song 2", path = "/music/rock/song2.mp3"),
            Song(id = 3L, title = "Song 3", path = "/music/pop/song3.mp3")
        )

        val folderEntities = songs.toFolderEntities()

        assertEquals(2, folderEntities.size)
        val rockPath = File("/music/rock/song1.mp3").parent
        val popPath = File("/music/pop/song3.mp3").parent
        val rockFolder = folderEntities.find { it.path == rockPath }
        val popFolder = folderEntities.find { it.path == popPath }

        assertNotNull(rockFolder)
        assertNotNull(popFolder)
        assertEquals(2, rockFolder?.songCount)
        assertEquals(1, popFolder?.songCount)
    }

    @Test
    fun `toFolderEntities filters songs without parent`() {
        val songs = listOf(
            Song(id = 1L, title = "Song 1", path = "song1.mp3"),
            Song(id = 2L, title = "Song 2", path = "/music/song2.mp3")
        )

        val folderEntities = songs.toFolderEntities()

        assertEquals(1, folderEntities.size)
        val expectedPath = File("/music/song2.mp3").parent
        assertEquals(expectedPath, folderEntities[0].path)
    }
}
