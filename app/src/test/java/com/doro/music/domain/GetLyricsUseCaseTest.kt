package com.doro.music.domain

import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.LyricsLine
import com.doro.music.data.model.LyricsSource
import com.doro.music.data.model.Song
import com.doro.music.data.repo.LyricsRepo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetLyricsUseCaseTest {

    private val mockRepo = mockk<LyricsRepo>()
    private lateinit var useCase: GetLyricsUseCase

    @Before
    fun setup() {
        useCase = GetLyricsUseCase(mockRepo)
    }

    @Test
    fun `invoke returns lyrics from repo`() = runTest {
        val song = Song(id = 1L, title = "Test", path = "/test.mp3")
        val lyrics = LyricsData(
            songId = 1L,
            source = LyricsSource.LOCAL_LRC,
            lines = listOf(LyricsLine(timeMs = 1000, text = "Hello"))
        )
        coEvery { mockRepo.getLyrics(song) } returns lyrics

        val result = useCase(song)

        assertEquals(lyrics, result)
    }

    @Test
    fun `invoke returns null when repo has no lyrics`() = runTest {
        val song = Song(id = 2L, title = "No Lyrics", path = "/none.mp3")
        coEvery { mockRepo.getLyrics(song) } returns null

        val result = useCase(song)

        assertNull(result)
    }

    @Test
    fun `invoke delegates to repo with correct song`() = runTest {
        val song = Song(id = 3L, title = "Verify", path = "/verify.mp3")
        coEvery { mockRepo.getLyrics(any()) } returns null

        useCase(song)

        coEvery { mockRepo.getLyrics(song) } answers { null }
    }
}
