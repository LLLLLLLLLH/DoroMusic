package com.doro.music.data.model

import junit.framework.TestCase.assertEquals
import org.junit.Test

class LyricsDataTest {

    @Test
    fun `LyricsData construction with all fields`() {
        val lines = listOf(
            LyricsLine(timeMs = 1000, text = "Line 1"),
            LyricsLine(timeMs = 5000, text = "Line 2")
        )
        val data = LyricsData(
            songId = 1L,
            source = LyricsSource.LOCAL_LRC,
            lines = lines,
            offset = 100L
        )

        assertEquals(1L, data.songId)
        assertEquals(LyricsSource.LOCAL_LRC, data.source)
        assertEquals(2, data.lines.size)
        assertEquals(100L, data.offset)
    }

    @Test
    fun `LyricsData default offset is 0`() {
        val data = LyricsData(
            songId = 1L,
            source = LyricsSource.CACHE,
            lines = emptyList()
        )

        assertEquals(0L, data.offset)
    }

    @Test
    fun `LyricsLine with negative timeMs indicates no sync`() {
        val line = LyricsLine(timeMs = -1, text = "Unsynced line")

        assertEquals(-1, line.timeMs)
        assertEquals("Unsynced line", line.text)
    }

    @Test
    fun `LyricsSource enum has 2 values`() {
        val sources = listOf(LyricsSource.LOCAL_LRC, LyricsSource.CACHE)
        assertEquals(2, sources.size)
        assertEquals("LOCAL_LRC", LyricsSource.LOCAL_LRC.name)
        assertEquals("CACHE", LyricsSource.CACHE.name)
    }
}