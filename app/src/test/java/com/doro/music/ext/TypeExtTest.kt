package com.doro.music.ext

import org.junit.Assert.*
import org.junit.Test

class TypeExtTest {

    @Test
    fun `formatDuration returns zero for non-positive values`() {
        assertEquals("00:00", 0L.formatDuration())
        assertEquals("00:00", (-1L).formatDuration())
    }

    @Test
    fun `formatDuration formats seconds correctly`() {
        assertEquals("00:01", 1000L.formatDuration())
        assertEquals("00:30", 30000L.formatDuration())
        assertEquals("00:59", 59000L.formatDuration())
    }

    @Test
    fun `formatDuration formats minutes correctly`() {
        assertEquals("01:00", 60000L.formatDuration())
        assertEquals("01:30", 90000L.formatDuration())
        assertEquals("12:34", 754000L.formatDuration())
    }

    @Test
    fun `formatDuration formats hours correctly`() {
        assertEquals("01:00:00", 3600000L.formatDuration())
        assertEquals("01:30:00", 5400000L.formatDuration())
        assertEquals("12:34:56", 45296000L.formatDuration())
    }

    @Test
    fun `formatDuration handles boundary values`() {
        assertEquals("00:00", 1L.formatDuration())
        assertEquals("00:00", 999L.formatDuration())
        assertEquals("00:01", 1000L.formatDuration())
        assertEquals("59:59", 3599000L.formatDuration())
        assertEquals("01:00:00", 3600000L.formatDuration())
    }
}
