package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class PlayerStateTest {

    @Test
    fun `PlayerState default values are correct`() {
        val state = PlayerState(currentIndex = 0, currentPosition = 0L)
        assertEquals(0, state.currentIndex)
        assertEquals(0L, state.currentPosition)
    }

    @Test
    fun `PlayerState with custom values`() {
        val state = PlayerState(currentIndex = 5, currentPosition = 120000L)
        assertEquals(5, state.currentIndex)
        assertEquals(120000L, state.currentPosition)
    }

    @Test
    fun `PlayerState equality works correctly`() {
        val state1 = PlayerState(currentIndex = 1, currentPosition = 1000L)
        val state2 = PlayerState(currentIndex = 1, currentPosition = 1000L)
        val state3 = PlayerState(currentIndex = 2, currentPosition = 1000L)

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `PlayerState copyWith creates modified copy`() {
        val original = PlayerState(currentIndex = 1, currentPosition = 5000L)
        val modified = original.copy(currentPosition = 10000L)

        assertEquals(1, modified.currentIndex)
        assertEquals(10000L, modified.currentPosition)
    }
}