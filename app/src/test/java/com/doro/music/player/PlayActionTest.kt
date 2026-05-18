package com.doro.music.player

import com.doro.music.data.model.SortMode
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayContext
import org.junit.Assert.*
import org.junit.Test

class PlayActionTest {

    @Test
    fun `Play Prev is singleton`() {
        val prev1 = PlayAction.Prev
        val prev2 = PlayAction.Prev
        assertEquals(prev1, prev2)
    }

    @Test
    fun `Play Next is singleton`() {
        val next1 = PlayAction.Next
        val next2 = PlayAction.Next
        assertEquals(next1, next2)
    }

    @Test
    fun `Play TogglePlay is singleton`() {
        val toggle1 = PlayAction.TogglePlay
        val toggle2 = PlayAction.TogglePlay
        assertEquals(toggle1, toggle2)
    }

    @Test
    fun `Play TogglePlayMode is singleton`() {
        val toggle1 = PlayAction.TogglePlayMode
        val toggle2 = PlayAction.TogglePlayMode
        assertEquals(toggle1, toggle2)
    }

    @Test
    fun `Play action data classes contain correct values`() {
        val playAction = PlayAction.Play(123L, PlayContext.All(SortMode.ARTIST))
        assertEquals(123L, playAction.songId)
        assertTrue(playAction.playContext is PlayContext.All)
    }

    @Test
    fun `SeekTo action contains correct position`() {
        val seekAction = PlayAction.SeekTo(5000L)
        assertEquals(5000L, seekAction.positionMs)
    }

    @Test
    fun `SeekToQueueItem action contains correct queueId`() {
        val seekAction = PlayAction.SeekToQueueItem(456L)
        assertEquals(456L, seekAction.queueId)
    }

    @Test
    fun `InsertSingle action contains correct songId`() {
        val insertAction = PlayAction.InsertSingle(789L)
        assertEquals(789L, insertAction.songId)
    }

    @Test
    fun `Remove action contains correct queueId`() {
        val removeAction = PlayAction.Remove(321L)
        assertEquals(321L, removeAction.queueId)
    }
}
