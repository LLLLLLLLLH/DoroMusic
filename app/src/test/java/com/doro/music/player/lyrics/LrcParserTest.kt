package com.doro.music.player.lyrics

import com.doro.music.data.model.LyricsSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LrcParserTest {

    @Test
    fun `parse standard lrc format`() {
        val lrc = "[00:12.00]First line\n[00:17.20]Second line\n[01:05.30]Third line"
        val result = LrcParser.parse(lrc)!!

        assertEquals(3, result.lines.size)
        assertEquals(12000L, result.lines[0].timeMs)
        assertEquals("First line", result.lines[0].text)
        assertEquals(17200L, result.lines[1].timeMs)
        assertEquals("Second line", result.lines[1].text)
        assertEquals(65300L, result.lines[2].timeMs)
        assertEquals("Third line", result.lines[2].text)
    }

    @Test
    fun `parse lrc with three-digit milliseconds`() {
        val lrc = "[00:12.345]Line with 3-digit ms"
        val result = LrcParser.parse(lrc)!!

        assertEquals(1, result.lines.size)
        assertEquals(12345L, result.lines[0].timeMs)
    }

    @Test
    fun `parse lrc with offset tag`() {
        val lrc = "[offset:+500]\n[00:10.00]Line with offset"
        val result = LrcParser.parse(lrc)!!

        assertEquals(500L, result.offset)
        assertEquals(1, result.lines.size)
        assertEquals(10000L, result.lines[0].timeMs)
    }

    @Test
    fun `parse lrc with negative offset`() {
        val lrc = "[offset:-200]\n[00:05.00]Line"
        val result = LrcParser.parse(lrc)!!

        assertEquals(-200L, result.offset)
    }

    @Test
    fun `parse lrc with multiple timestamps per line`() {
        val lrc = "[00:12.00][01:30.00]Chorus line"
        val result = LrcParser.parse(lrc)!!

        assertEquals(2, result.lines.size)
        assertEquals(12000L, result.lines[0].timeMs)
        assertEquals(90000L, result.lines[1].timeMs)
        assertEquals("Chorus line", result.lines[0].text)
        assertEquals("Chorus line", result.lines[1].text)
    }

    @Test
    fun `return null for empty content`() {
        assertNull(LrcParser.parse(""))
    }

    @Test
    fun `return null for content without timestamps`() {
        assertNull(LrcParser.parse("no timestamps here\njust plain text"))
    }

    @Test
    fun `skip lines with empty text after timestamp`() {
        val lrc = "[00:05.00]\n[00:10.00]Real line"
        val result = LrcParser.parse(lrc)!!

        assertEquals(1, result.lines.size)
        assertEquals("Real line", result.lines[0].text)
    }

    @Test
    fun `lines are sorted by time`() {
        val lrc = "[01:00.00]Second\n[00:30.00]First\n[02:00.00]Third"
        val result = LrcParser.parse(lrc)!!

        assertEquals("First", result.lines[0].text)
        assertEquals("Second", result.lines[1].text)
        assertEquals("Third", result.lines[2].text)
    }

    @Test
    fun `parse ignores metadata tags`() {
        val lrc = "[ar:Artist]\n[ti:Title]\n[al:Album]\n[00:05.00]Actual lyric"
        val result = LrcParser.parse(lrc)!!

        assertEquals(1, result.lines.size)
        assertEquals("Actual lyric", result.lines[0].text)
    }

    @Test
    fun `default source is LOCAL_LRC`() {
        val lrc = "[00:01.00]Test"
        val result = LrcParser.parse(lrc)!!

        assertEquals(LyricsSource.LOCAL_LRC, result.source)
    }

    @Test
    fun `default songId is 0`() {
        val lrc = "[00:01.00]Test"
        val result = LrcParser.parse(lrc)!!

        assertEquals(0L, result.songId)
    }

    @Test
    fun `parse three-digit minutes`() {
        val lrc = "[100:00.00]Very long song"
        val result = LrcParser.parse(lrc)!!

        assertEquals(6000000L, result.lines[0].timeMs)
    }
}
