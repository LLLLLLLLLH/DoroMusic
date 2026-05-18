package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class AppSettingsTest {

    @Test
    fun `default AppSettings values are correct`() {
        val settings = AppSettings()
        assertEquals(PlayMode.REPEAT, settings.defaultPlayMode)
        assertEquals(DarkThemeMode.SYSTEM, settings.darkTheme)
        assertEquals(30, settings.minDurationFilter)
    }

    @Test
    fun `AppSettings with custom values`() {
        val settings = AppSettings(
            defaultPlayMode = PlayMode.SHUFFLE,
            darkTheme = DarkThemeMode.DARK,
            minDurationFilter = 60
        )
        assertEquals(PlayMode.SHUFFLE, settings.defaultPlayMode)
        assertEquals(DarkThemeMode.DARK, settings.darkTheme)
        assertEquals(60, settings.minDurationFilter)
    }

    @Test
    fun `AppSettings copyWith creates modified copy`() {
        val original = AppSettings()
        val modified = original.copy(minDurationFilter = 120)

        assertEquals(PlayMode.REPEAT, modified.defaultPlayMode)
        assertEquals(DarkThemeMode.SYSTEM, modified.darkTheme)
        assertEquals(120, modified.minDurationFilter)
    }

    @Test
    fun `AppSettings equality works correctly`() {
        val settings1 = AppSettings()
        val settings2 = AppSettings()
        val settings3 = AppSettings(minDurationFilter = 60)

        assertEquals(settings1, settings2)
        assertNotEquals(settings1, settings3)
    }
}