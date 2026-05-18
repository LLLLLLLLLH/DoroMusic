package com.doro.music.data.model

import com.doro.music.ext.orDefault
import org.junit.Assert.*
import org.junit.Test

class PlayModeTest {

    @Test
    fun `PlayMode enum values are correct`() {
        assertEquals(3, PlayMode.entries.size)
        assertTrue(PlayMode.entries.contains(PlayMode.REPEAT))
        assertTrue(PlayMode.entries.contains(PlayMode.SHUFFLE))
        assertTrue(PlayMode.entries.contains(PlayMode.REPEAT_ONE))
    }

    @Test
    fun `parses valid PlayMode from String`() {
        assertEquals(PlayMode.REPEAT, PlayMode.pares("REPEAT"))
        assertEquals(PlayMode.SHUFFLE, PlayMode.pares("SHUFFLE"))
        assertEquals(PlayMode.REPEAT_ONE, PlayMode.pares("REPEAT_ONE"))
    }

    @Test
    fun `parses returns default for invalid String`() {
        assertEquals(PlayMode.REPEAT, PlayMode.pares("INVALID"))
        assertEquals(PlayMode.REPEAT, PlayMode.pares(""))
        assertEquals(PlayMode.REPEAT, PlayMode.pares(null))
    }

    @Test
    fun `orDefault returns value when not null`() {
        assertEquals(PlayMode.SHUFFLE, PlayMode.SHUFFLE.orDefault(PlayMode.REPEAT))
    }

    @Test
    fun `orDefault returns default when null`() {
        val nullPlayMode: PlayMode? = null
        assertEquals(PlayMode.REPEAT, nullPlayMode.orDefault(PlayMode.REPEAT))
    }
}
