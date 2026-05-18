package com.doro.music.player.model

import com.doro.music.data.model.PlayMode
import com.doro.music.data.model.PlaybackState
import org.junit.Assert.*
import org.junit.Test

class PlayUiStateTest {

    @Test
    fun `PlayUiState default values are correct`() {
        val state = PlayUiState()

        assertNull(state.currentQueueId)
        assertNull(state.currentSongId)
        assertEquals(PlayMode.REPEAT, state.playMode)
        assertEquals(PlaybackState.IDLE, state.playbackState)
    }

    @Test
    fun `PlayUiState with custom values`() {
        val state = PlayUiState(
            currentQueueId = 100L,
            currentSongId = 200L,
            playMode = PlayMode.SHUFFLE,
            playbackState = PlaybackState.PLAYING
        )

        assertEquals(100L, state.currentQueueId)
        assertEquals(200L, state.currentSongId)
        assertEquals(PlayMode.SHUFFLE, state.playMode)
        assertEquals(PlaybackState.PLAYING, state.playbackState)
    }

    @Test
    fun `PlayUiState Empty is singleton with default values`() {
        val empty1 = PlayUiState.Empty
        val empty2 = PlayUiState.Empty

        assertEquals(empty1, empty2)
        assertEquals(PlayUiState(), empty1)
    }

    @Test
    fun `PlayUiState copyWith creates modified copy`() {
        val original = PlayUiState(
            currentQueueId = 1L,
            currentSongId = 2L,
            playMode = PlayMode.REPEAT,
            playbackState = PlaybackState.PAUSED
        )

        val modified = original.copy(playbackState = PlaybackState.PLAYING)

        assertEquals(1L, modified.currentQueueId)
        assertEquals(2L, modified.currentSongId)
        assertEquals(PlayMode.REPEAT, modified.playMode)
        assertEquals(PlaybackState.PLAYING, modified.playbackState)
    }

    @Test
    fun `PlayUiState equality works correctly`() {
        val state1 = PlayUiState(currentQueueId = 1L, currentSongId = 2L)
        val state2 = PlayUiState(currentQueueId = 1L, currentSongId = 2L)
        val state3 = PlayUiState(currentQueueId = 1L, currentSongId = 3L)

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }
}
