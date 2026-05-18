package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun `PlaybackState enum values are correct`() {
        assertEquals(4, PlaybackState.entries.size)
        assertTrue(PlaybackState.entries.contains(PlaybackState.IDLE))
        assertTrue(PlaybackState.entries.contains(PlaybackState.PLAYING))
        assertTrue(PlaybackState.entries.contains(PlaybackState.PAUSED))
        assertTrue(PlaybackState.entries.contains(PlaybackState.ERROR))
    }
}
