package com.doro.music.player.model

import com.doro.music.data.model.PlayMode
import org.junit.Assert.*
import org.junit.Test

class PersistedPlayStateTest {

    @Test
    fun `default values are correct`() {
        val state = PersistedPlayState()
        assertEquals(0L, state.currentQueueId)
        assertEquals(0L, state.currentSongId)
        assertEquals(0L, state.positionMs)
        assertEquals(PlayMode.REPEAT, state.playMode)
        assertEquals(0L, state.shuffleSeed)
    }

    @Test
    fun `custom values are set correctly`() {
        val state = PersistedPlayState(
            currentQueueId = 10L,
            currentSongId = 20L,
            positionMs = 30000L,
            playMode = PlayMode.SHUFFLE,
            shuffleSeed = 42L
        )
        assertEquals(10L, state.currentQueueId)
        assertEquals(20L, state.currentSongId)
        assertEquals(30000L, state.positionMs)
        assertEquals(PlayMode.SHUFFLE, state.playMode)
        assertEquals(42L, state.shuffleSeed)
    }

    @Test
    fun `equality works correctly`() {
        val state1 = PersistedPlayState(currentQueueId = 1L, currentSongId = 2L)
        val state2 = PersistedPlayState(currentQueueId = 1L, currentSongId = 2L)
        val state3 = PersistedPlayState(currentQueueId = 1L, currentSongId = 3L)

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `copyWith creates modified copy`() {
        val original = PersistedPlayState(currentQueueId = 1L, positionMs = 5000L)
        val modified = original.copy(positionMs = 10000L)

        assertEquals(1L, modified.currentQueueId)
        assertEquals(10000L, modified.positionMs)
    }
}