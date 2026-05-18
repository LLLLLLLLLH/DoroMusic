package com.doro.music.ext

import org.junit.Assert.*
import org.junit.Test

class CommonExtTest {

    @Test
    fun `orDefault with String works correctly`() {
        val value = "hello"
        assertEquals("hello", value.orDefault("default"))

        val nullValue: String? = null
        assertEquals("default", nullValue.orDefault("default"))
    }

    @Test
    fun `orDefault with Int works correctly`() {
        val value = 42
        assertEquals(42, value.orDefault(0))

        val nullValue: Int? = null
        assertEquals(0, nullValue.orDefault(0))
    }

    @Test
    fun `orDefault with Long works correctly`() {
        val value = 100L
        assertEquals(100L, value.orDefault(0L))

        val nullValue: Long? = null
        assertEquals(0L, nullValue.orDefault(0L))
    }

    @Test
    fun `orDefault with Boolean works correctly`() {
        val value = true
        assertTrue(value.orDefault(false))

        val nullValue: Boolean? = null
        assertFalse(nullValue.orDefault(false))
    }

    @Test
    fun `orDefault with List works correctly`() {
        val value: List<String> = listOf("a", "b")
        assertEquals(2, value.orDefault(emptyList()).size)

        @Suppress("USELESS_IS_CHECK")
        val nullValue: List<String>? = null
        assertTrue(nullValue.orDefault(emptyList()).isEmpty())
    }
}
