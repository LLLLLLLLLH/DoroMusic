package com.doro.music.player.util

import org.junit.Assert.*
import org.junit.Test

class FractionalIndexerTest {

    // ==================== generateBetween ====================

    @Test
    fun `generateBetween with both null returns default a0`() {
        assertEquals("a0", FractionalIndexer.generateBetween(null, null))
    }

    @Test
    fun `generateBetween with null before generates before after`() {
        val result = FractionalIndexer.generateBetween(null, "c0")
        assertTrue(result < "c0")
    }

    @Test
    fun `generateBetween with null after generates after before`() {
        val result = FractionalIndexer.generateBetween("a0", null)
        assertTrue(result > "a0")
    }

    @Test
    fun `generateBetween with both non-null generates between`() {
        val result = FractionalIndexer.generateBetween("a0", "c0")
        assertTrue(result > "a0")
        assertTrue(result < "c0")
    }

    @Test
    fun `generateBetween with adjacent prefixes generates midpoint`() {
        val result = FractionalIndexer.generateBetween("a0", "a2")
        assertEquals("a1", result)
    }

    @Test
    fun `generateBetween with same prefix different suffix`() {
        val result = FractionalIndexer.generateBetween("ab", "ac")
        assertTrue(result > "ab")
        assertTrue(result < "ac")
    }

    @Test
    fun `generateBetween throws when before greater than after`() {
        try {
            FractionalIndexer.generateBetween("c0", "a0")
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun `generateBetween with after being prefix of before returns before plus mid`() {
        val result = FractionalIndexer.generateBetween("a0", "a0a")
        assertTrue(result > "a0")
        assertTrue(result < "a0a")
    }

    @Test
    fun `generateBetween with close values appends mid character`() {
        val result = FractionalIndexer.generateBetween("a0", "a1")
        assertTrue(result > "a0")
        assertTrue(result < "a1")
    }

    // ==================== generateBefore ====================

    @Test
    fun `generateBefore with first char having high index decrements it`() {
        val result = FractionalIndexer.generateBetween(null, "c0")
        assertNotNull(result)
        assertTrue(result < "c0")
    }

    @Test
    fun `generateBefore with first char at index 1 recurses`() {
        val result = FractionalIndexer.generateBetween(null, "1Z")
        assertNotNull(result)
        assertTrue(result < "1Z")
    }

    // ==================== generateAfter ====================

    @Test
    fun `generateAfter with last char having room increments it`() {
        val result = FractionalIndexer.generateBetween("a0", null)
        assertNotNull(result)
        assertTrue(result > "a0")
    }

    @Test
    fun `generateAfter with last char at max appends base char`() {
        val result = FractionalIndexer.generateBetween("aZ", null)
        assertNotNull(result)
        assertTrue(result > "aZ")
    }

    // ==================== generateInitialList ====================

    @Test
    fun `generateInitialList with zero count returns empty list`() {
        assertTrue(FractionalIndexer.generateInitialList(0).isEmpty())
    }

    @Test
    fun `generateInitialList with count 1 returns single element`() {
        val result = FractionalIndexer.generateInitialList(1)
        assertEquals(1, result.size)
        assertEquals("a0", result[0])
    }

    @Test
    fun `generateInitialList with count 3 returns correct list`() {
        val result = FractionalIndexer.generateInitialList(3)
        assertEquals(listOf("a0", "a1", "a2"), result)
    }

    @Test
    fun `generateInitialList generates sequential indices`() {
        val result = FractionalIndexer.generateInitialList(10)
        for (i in result.indices) {
            assertEquals("a$i", result[i])
        }
    }

    // ==================== generateBetweenList ====================

    @Test
    fun `generateBetweenList starts with before as first element`() {
        // generateSequence(before) { ... } starts with 'before' itself
        val result = FractionalIndexer.generateBetweenList("a0", "a5", 1)
        assertEquals(1, result.size)
        assertEquals("a0", result[0])
    }

    @Test
    fun `generateBetweenList with count 3 returns ordered elements`() {
        val result = FractionalIndexer.generateBetweenList("a0", "a5", 3)
        assertEquals(3, result.size)
        assertEquals("a0", result[0])
        for (i in 1 until result.size) {
            assertTrue(result[i] > result[i - 1])
            assertTrue(result[i] < "a5")
        }
    }

    @Test
    fun `generateBetweenList with null after generates after before`() {
        val result = FractionalIndexer.generateBetweenList("a0", null, 3)
        assertEquals(3, result.size)
        assertEquals("a0", result[0])
        for (i in 1 until result.size) {
            assertTrue(result[i] > result[i - 1])
        }
    }

    @Test
    fun `generateBetweenList with count 0 returns empty list`() {
        assertTrue(FractionalIndexer.generateBetweenList("a0", "a2", 0).isEmpty())
    }

    // ==================== Edge cases ====================

    @Test
    fun `generateBetween produces sorted sequence for multiple calls`() {
        val first = FractionalIndexer.generateBetween("a0", "a5")
        val second = FractionalIndexer.generateBetween(first, "a5")
        val third = FractionalIndexer.generateBetween(second, "a5")

        assertTrue("a0" < first)
        assertTrue(first < second)
        assertTrue(second < third)
        assertTrue(third < "a5")
    }

    @Test
    fun `generateBetween with single char strings`() {
        val result = FractionalIndexer.generateBetween("a", "c")
        assertTrue(result > "a")
        assertTrue(result < "c")
    }

    @Test
    fun `generateBetween with very close base62 chars`() {
        val result = FractionalIndexer.generateBetween("0", "2")
        assertEquals("1", result)
    }

    @Test
    fun `generateBetweenList produces monotonically increasing sequence`() {
        // Use a wide range where generateBetween can produce distinct values
        val result = FractionalIndexer.generateBetweenList("a0", "z0", 5)
        for (i in 1 until result.size) {
            assertTrue("Expected ${result[i]} > ${result[i-1]}", result[i] > result[i - 1])
        }
    }
}