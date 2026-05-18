package com.doro.music.player

import com.doro.music.data.model.SortMode
import com.doro.music.player.model.PlayContext
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayContextTest {

    @Test
    fun `PlayContext All has correct sortMode`() {
        val context = PlayContext.All(SortMode.TITLE)
        assertEquals(SortMode.TITLE, context.sortMode)
    }

    @Test
    fun `PlayContext Artist has correct values`() {
        val context = PlayContext.Artist("Test Artist", SortMode.ARTIST)
        assertEquals("Test Artist", context.artist)
        assertEquals(SortMode.ARTIST, context.sortMode)
    }

    @Test
    fun `PlayContext Folder has correct values`() {
        val context = PlayContext.Folder("/music/rock", SortMode.DATE_ADDED)
        assertEquals("/music/rock", context.path)
        assertEquals(SortMode.DATE_ADDED, context.sortMode)
    }

    @Test
    fun `PlayContext Playlist has correct values`() {
        val context = PlayContext.Playlist(123L, SortMode.TITLE)
        assertEquals(123L, context.playlistId)
        assertEquals(SortMode.TITLE, context.sortMode)
    }

    @Test
    fun `PlayContext Search has correct values`() {
        val context = PlayContext.Search("test query", SortMode.ARTIST)
        assertEquals("test query", context.keyword)
        assertEquals(SortMode.ARTIST, context.sortMode)
    }
}
