package com.doro.music.data.repo

import com.doro.music.data.db.dao.ArtistDao
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArtistRepoTest {

    private val mockArtistDao = mockk<ArtistDao>()
    private lateinit var repo: ArtistRepo

    @Before
    fun setup() {
        repo = ArtistRepo(mockArtistDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getArtistCount delegates to DAO`() = runTest {
        every { mockArtistDao.getArtistCount() } returns flowOf(5)

        val count = repo.getArtistCount().first()
        assertEquals(5, count)
    }

    @Test
    fun `getArtistCount returns zero when no artists`() = runTest {
        every { mockArtistDao.getArtistCount() } returns flowOf(0)

        val count = repo.getArtistCount().first()
        assertEquals(0, count)
    }
}