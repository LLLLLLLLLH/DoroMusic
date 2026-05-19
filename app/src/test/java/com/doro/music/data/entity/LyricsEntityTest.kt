package com.doro.music.data.entity

import com.doro.music.data.db.entities.LyricsEntity
import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.LyricsLine
import com.doro.music.data.model.LyricsSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LyricsEntityTest {

    @Test
    fun `LyricsEntity default values are correct`() {
        val entity = LyricsEntity(
            songId = 1L,
            lrcContent = "[00:01.00]Hello",
            source = LyricsSource.LOCAL_LRC.name
        )

        assertEquals(0L, entity.id)
        assertEquals(1L, entity.songId)
        assertEquals("[00:01.00]Hello", entity.lrcContent)
        assertEquals("LOCAL_LRC", entity.source)
        assertEquals(0L, entity.offset)
        assert(entity.fetchedAt > 0)
    }

    @Test
    fun `LyricsEntity with all fields specified`() {
        val entity = LyricsEntity(
            id = 42L,
            songId = 10L,
            lrcContent = "[00:05.00]World",
            source = LyricsSource.CACHE.name,
            fetchedAt = 1700000000000L,
            offset = 200L
        )

        assertEquals(42L, entity.id)
        assertEquals(10L, entity.songId)
        assertEquals("[00:05.00]World", entity.lrcContent)
        assertEquals("CACHE", entity.source)
        assertEquals(1700000000000L, entity.fetchedAt)
        assertEquals(200L, entity.offset)
    }

    @Test
    fun `LyricsEntity copy preserves values`() {
        val original = LyricsEntity(
            id = 1L,
            songId = 5L,
            lrcContent = "content",
            source = LyricsSource.CACHE.name
        )

        val modified = original.copy(offset = 100L)

        assertEquals(1L, modified.id)
        assertEquals(5L, modified.songId)
        assertEquals("content", modified.lrcContent)
        assertEquals("CACHE", modified.source)
        assertEquals(100L, modified.offset)
    }
}

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
