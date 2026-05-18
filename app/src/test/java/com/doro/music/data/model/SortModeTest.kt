package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class SortModeTest {

    @Test
    fun `SortMode enum values are correct`() {
        assertEquals(3, SortMode.entries.size)
        assertTrue(SortMode.entries.contains(SortMode.TITLE))
        assertTrue(SortMode.entries.contains(SortMode.ARTIST))
        assertTrue(SortMode.entries.contains(SortMode.DATE_ADDED))
    }

    @Test
    fun `SortMode has correct labelResId`() {
        assertNotNull(SortMode.TITLE.labelResId)
        assertNotNull(SortMode.ARTIST.labelResId)
        assertNotNull(SortMode.DATE_ADDED.labelResId)
    }

    @Test
    fun `SortMode entries contains all expected values`() {
        val values = SortMode.entries
        assertEquals(3, values.size)
        assertTrue(values.contains(SortMode.TITLE))
        assertTrue(values.contains(SortMode.ARTIST))
        assertTrue(values.contains(SortMode.DATE_ADDED))
    }
}
