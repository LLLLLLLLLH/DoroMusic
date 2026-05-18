package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class PlayerActionTest {

    @Test
    fun `PlayerAction data objects are singletons`() {
        assertEquals(PlayerAction.Previous, PlayerAction.Previous)
        assertEquals(PlayerAction.TogglePlayPause, PlayerAction.TogglePlayPause)
        assertEquals(PlayerAction.Next, PlayerAction.Next)
        assertEquals(PlayerAction.TogglePlayMode, PlayerAction.TogglePlayMode)
        assertEquals(PlayerAction.TogglePlayerView, PlayerAction.TogglePlayerView)
        assertEquals(PlayerAction.TogglePlayerSheet, PlayerAction.TogglePlayerSheet)
        assertEquals(PlayerAction.TogglePlayQueue, PlayerAction.TogglePlayQueue)
    }

    @Test
    fun `PlayerAction SeekTo contains correct position`() {
        val action = PlayerAction.SeekTo(5000L)
        assertEquals(5000L, action.positionMs)
    }
}