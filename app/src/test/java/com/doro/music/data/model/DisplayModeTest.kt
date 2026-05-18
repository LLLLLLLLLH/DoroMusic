package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class DisplayModeTest {

    @Test
    fun `DisplayMode enum values are correct`() {
        assertEquals(3, DisplayMode.entries.size)
        assertTrue(DisplayMode.entries.contains(DisplayMode.COMPACT))
        assertTrue(DisplayMode.entries.contains(DisplayMode.LIST))
        assertTrue(DisplayMode.entries.contains(DisplayMode.GRID))
    }

    @Test
    fun `DisplayMode has correct labelResId`() {
        assertNotNull(DisplayMode.COMPACT.labelResId)
        assertNotNull(DisplayMode.LIST.labelResId)
        assertNotNull(DisplayMode.GRID.labelResId)
    }

    @Test
    fun `DisplayMode entries contains all expected values`() {
        val values = DisplayMode.entries
        assertEquals(3, values.size)
    }
}
