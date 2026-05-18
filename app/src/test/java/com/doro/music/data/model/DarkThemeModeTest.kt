package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class DarkThemeModeTest {

    @Test
    fun `DarkThemeMode enum has correct values`() {
        assertEquals(3, DarkThemeMode.entries.size)
        assertTrue(DarkThemeMode.entries.contains(DarkThemeMode.SYSTEM))
        assertTrue(DarkThemeMode.entries.contains(DarkThemeMode.LIGHT))
        assertTrue(DarkThemeMode.entries.contains(DarkThemeMode.DARK))
    }

    @Test
    fun `DarkThemeMode enum values are distinct`() {
        assertNotEquals(DarkThemeMode.SYSTEM, DarkThemeMode.LIGHT)
        assertNotEquals(DarkThemeMode.SYSTEM, DarkThemeMode.DARK)
        assertNotEquals(DarkThemeMode.LIGHT, DarkThemeMode.DARK)
    }

    @Test
    fun `DarkThemeMode valueOf returns correct enum`() {
        assertEquals(DarkThemeMode.SYSTEM, DarkThemeMode.valueOf("SYSTEM"))
        assertEquals(DarkThemeMode.LIGHT, DarkThemeMode.valueOf("LIGHT"))
        assertEquals(DarkThemeMode.DARK, DarkThemeMode.valueOf("DARK"))
    }
}