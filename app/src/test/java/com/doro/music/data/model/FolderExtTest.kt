package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class FolderExtTest {

    @Test
    fun `activeFolders filters out excluded folders`() {
        val folders = listOf(
            Folder(path = "/music/rock", songCount = 5, excluded = false),
            Folder(path = "/music/pop", songCount = 3, excluded = true),
            Folder(path = "/music/jazz", songCount = 2, excluded = false)
        )

        val active = folders.activeFolders()
        assertEquals(2, active.size)
        assertEquals("/music/rock", active[0].path)
        assertEquals("/music/jazz", active[1].path)
    }

    @Test
    fun `activeFolders returns all when none excluded`() {
        val folders = listOf(
            Folder(path = "/a", songCount = 1),
            Folder(path = "/b", songCount = 2)
        )

        val active = folders.activeFolders()
        assertEquals(2, active.size)
    }

    @Test
    fun `activeFolders returns empty when all excluded`() {
        val folders = listOf(
            Folder(path = "/a", songCount = 1, excluded = true),
            Folder(path = "/b", songCount = 2, excluded = true)
        )

        val active = folders.activeFolders()
        assertTrue(active.isEmpty())
    }

    @Test
    fun `excludedFolders returns only excluded folders`() {
        val folders = listOf(
            Folder(path = "/music/rock", songCount = 5, excluded = false),
            Folder(path = "/music/pop", songCount = 3, excluded = true),
            Folder(path = "/music/jazz", songCount = 2, excluded = true)
        )

        val excluded = folders.excludedFolders()
        assertEquals(2, excluded.size)
        assertEquals("/music/pop", excluded[0].path)
        assertEquals("/music/jazz", excluded[1].path)
    }

    @Test
    fun `Folder name extracts last segment of path`() {
        val folder = Folder(path = "/storage/emulated/0/Music/Rock")
        assertEquals("Rock", folder.name)
    }

    @Test
    fun `Folder name with simple path`() {
        val folder = Folder(path = "Music")
        assertEquals("Music", folder.name)
    }

    @Test
    fun `Folder default values are correct`() {
        val folder = Folder(path = "/test")
        assertEquals("/test", folder.path)
        assertEquals(0, folder.songCount)
        assertFalse(folder.excluded)
    }
}