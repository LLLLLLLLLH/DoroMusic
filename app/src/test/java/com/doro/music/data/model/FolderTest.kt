package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class FolderTest {

    @Test
    fun `Folder with default values`() {
        val folder = Folder(path = "/storage/music")

        assertEquals("/storage/music", folder.path)
        assertEquals(0, folder.songCount)
        assertFalse(folder.excluded)
    }

    @Test
    fun `Folder with custom values`() {
        val folder = Folder(
            path = "/storage/music/pop",
            songCount = 25,
            excluded = true
        )

        assertEquals("/storage/music/pop", folder.path)
        assertEquals(25, folder.songCount)
        assertTrue(folder.excluded)
    }

    @Test
    fun `Folder name extracts correctly from path`() {
        val folder = Folder(path = "/storage/music/Rock Songs")
        assertEquals("Rock Songs", folder.name)
    }

    @Test
    fun `Folder name with nested path`() {
        val folder = Folder(path = "/storage/music/2024/Best Hits")
        assertEquals("Best Hits", folder.name)
    }

    @Test
    fun `Folder equality works correctly`() {
        val folder1 = Folder(path = "/music", songCount = 10)
        val folder2 = Folder(path = "/music", songCount = 10)
        val folder3 = Folder(path = "/music", songCount = 20)

        assertEquals(folder1, folder2)
        assertNotEquals(folder1, folder3)
    }

    @Test
    fun `Folder copyWith creates modified copy`() {
        val original = Folder(path = "/original", songCount = 5, excluded = false)
        val modified = original.copy(excluded = true)

        assertEquals("/original", modified.path)
        assertEquals(5, modified.songCount)
        assertTrue(modified.excluded)
    }
}
