package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class AddSongResultTest {

    @Test
    fun `AddSongResult Success is distinct`() {
        val result1 = AddSongResult.Success
        val result2 = AddSongResult.Success
        assertEquals(result1, result2)
    }

    @Test
    fun `AddSongResult AlreadyExists is distinct`() {
        val result1 = AddSongResult.AlreadyExists
        val result2 = AddSongResult.AlreadyExists
        assertEquals(result1, result2)
    }

    @Test
    fun `AddSongResult Failed is distinct`() {
        val result1 = AddSongResult.Failed
        val result2 = AddSongResult.Failed
        assertEquals(result1, result2)
    }

}
