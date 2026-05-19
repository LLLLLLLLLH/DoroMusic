package com.doro.music.player.lyrics

import com.doro.music.data.model.LyricsSource
import com.doro.music.data.model.Song
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class LrcFileLoaderTest {

    private val loader = LrcFileLoader()

    @Test
    fun `loadForSong returns parsed lyrics when lrc file exists`() = runTest {
        val tempDir = System.getProperty("java.io.tmpdir")
        val ts = System.currentTimeMillis()
        val audioFile = File(tempDir, "test_song_${ts}.mp3")
        val lrcFile = File(tempDir, "test_song_${ts}.lrc")

        try {
            audioFile.writeText("fake audio")
            lrcFile.writeText("[00:05.00]Hello World\n[00:10.00]Second Line")

            val song = Song(id = 1L, title = "Test", path = audioFile.absolutePath)
            val result = loader.loadForSong(song)

            assertEquals(LyricsSource.LOCAL_LRC, result!!.source)
            assertEquals(1L, result.songId)
            assertEquals(2, result.lines.size)
            assertEquals("Hello World", result.lines[0].text)
            assertEquals(5000L, result.lines[0].timeMs)
            assertEquals("Second Line", result.lines[1].text)
            assertEquals(10000L, result.lines[1].timeMs)
        } finally {
            audioFile.delete()
            lrcFile.delete()
        }
    }

    @Test
    fun `loadForSong returns null when no lrc file exists`() = runTest {
        val song = Song(id = 2L, title = "No Lrc", path = "/nonexistent/path/song.mp3")

        val result = loader.loadForSong(song)

        assertNull(result)
    }

    @Test
    fun `loadForSong returns null when lrc file has no valid content`() = runTest {
        val tempDir = System.getProperty("java.io.tmpdir")
        val ts = System.currentTimeMillis()
        val audioFile = File(tempDir, "empty_lrc_${ts}.mp3")
        val lrcFile = File(tempDir, "empty_lrc_${ts}.lrc")

        try {
            audioFile.writeText("fake audio")
            lrcFile.writeText("no timestamps here\njust plain text")

            val song = Song(id = 3L, title = "Empty Lrc", path = audioFile.absolutePath)
            val result = loader.loadForSong(song)

            assertNull(result)
        } finally {
            audioFile.delete()
            lrcFile.delete()
        }
    }

    @Test
    fun `loadForSong finds lrc with case-insensitive match`() = runTest {
        val tempDir = System.getProperty("java.io.tmpdir")
        val dir = File(tempDir, "lrc_test_${System.currentTimeMillis()}")
        dir.mkdirs()

        try {
            val audioFile = File(dir, "MySong.mp3")
            val lrcFile = File(dir, "MySong.LRC")
            audioFile.writeText("fake audio")
            lrcFile.writeText("[00:01.00]Case insensitive")

            val song = Song(id = 4L, title = "CaseTest", path = audioFile.absolutePath)
            val result = loader.loadForSong(song)

            assertEquals(LyricsSource.LOCAL_LRC, result!!.source)
            assertEquals("Case insensitive", result.lines[0].text)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `loadForSong returns null when parentFile is null`() = runTest {
        // A path without a directory component results in parentFile being null
        val song = Song(id = 5L, title = "NoParent", path = "song.mp3")

        val result = loader.loadForSong(song)

        assertNull(result)
    }

    @Test
    fun `loadForSong returns null when directory has no lrc files`() = runTest {
        val tempDir = System.getProperty("java.io.tmpdir")
        val dir = File(tempDir, "no_lrc_dir_${System.currentTimeMillis()}")
        dir.mkdirs()

        try {
            val audioFile = File(dir, "track.mp3")
            // Create a non-lrc file with same base name
            val txtFile = File(dir, "track.txt")
            audioFile.writeText("fake audio")
            txtFile.writeText("not a lyric file")

            val song = Song(id = 6L, title = "NoLrc", path = audioFile.absolutePath)
            val result = loader.loadForSong(song)

            assertNull(result)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `loadForSong returns null when same base name but wrong extension`() = runTest {
        val tempDir = System.getProperty("java.io.tmpdir")
        val dir = File(tempDir, "wrong_ext_${System.currentTimeMillis()}")
        dir.mkdirs()

        try {
            val audioFile = File(dir, "music.mp3")
            // Same base name but different extension
            val wrongExtFile = File(dir, "music.txt")
            val anotherWrongExtFile = File(dir, "music.mp3.bak")
            audioFile.writeText("fake audio")
            wrongExtFile.writeText("wrong extension")
            anotherWrongExtFile.writeText("also wrong")

            val song = Song(id = 7L, title = "WrongExt", path = audioFile.absolutePath)
            val result = loader.loadForSong(song)

            assertNull(result)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `loadForSong finds lrc with case-insensitive base name match`() = runTest {
        val tempDir = System.getProperty("java.io.tmpdir")
        val dir = File(tempDir, "base_name_case_${System.currentTimeMillis()}")
        dir.mkdirs()

        try {
            // Audio file with uppercase base name
            val audioFile = File(dir, "MYSONG.mp3")
            // LRC file with lowercase base name
            val lrcFile = File(dir, "mySong.lrc")
            audioFile.writeText("fake audio")
            lrcFile.writeText("[00:03.00]Base name case test")

            val song = Song(id = 8L, title = "BaseNameCase", path = audioFile.absolutePath)
            val result = loader.loadForSong(song)

            assertEquals(LyricsSource.LOCAL_LRC, result!!.source)
            assertEquals("Base name case test", result.lines[0].text)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `loadForSong finds first matching lrc when multiple exist`() = runTest {
        val tempDir = System.getProperty("java.io.tmpdir")
        val dir = File(tempDir, "multi_lrc_${System.currentTimeMillis()}")
        dir.mkdirs()

        try {
            val audioFile = File(dir, "song.mp3")
            audioFile.writeText("fake audio")
            // Create multiple lrc files with same base name but different case extensions
            val lrcFile1 = File(dir, "song.lrc")
            val lrcFile2 = File(dir, "song.LRC")
            lrcFile1.writeText("[00:01.00]First lrc")
            lrcFile2.writeText("[00:02.00]Second lrc")

            val song = Song(id = 9L, title = "MultiLrc", path = audioFile.absolutePath)
            val result = loader.loadForSong(song)

            // Should find one of them (firstOrNull behavior)
            assertEquals(LyricsSource.LOCAL_LRC, result!!.source)
        } finally {
            dir.deleteRecursively()
        }
    }
}