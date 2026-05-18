package com.doro.music.domain

import com.doro.music.data.model.Song
import com.doro.music.data.repo.MainRepo
import com.doro.music.player.util.ScanResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ScanMusicUseCaseTest {

    private val mockRepo = mockk<MainRepo>()
    private val useCase = ScanMusicUseCase(mockRepo)

    @Test
    fun `invoke returns ScanResult Success from repo`() = runTest {
        val songs = listOf(
            Song(id = 1L, title = "Song 1", path = "/music/song1.mp3"),
            Song(id = 2L, title = "Song 2", path = "/music/song2.mp3")
        )
        coEvery { mockRepo.scan() } returns ScanResult.Success(songs)

        val result = useCase()

        assertTrue(result is ScanResult.Success)
        assertEquals(2, (result as ScanResult.Success).songs.size)
    }

    @Test
    fun `invoke returns ScanResult Error when scan fails`() = runTest {
        coEvery { mockRepo.scan() } returns ScanResult.Error(Exception("Scan failed"))

        val result = useCase()

        assertTrue(result is ScanResult.Error)
    }

    @Test
    fun `invoke returns success with empty song list`() = runTest {
        coEvery { mockRepo.scan() } returns ScanResult.Success(emptyList())

        val result = useCase()

        assertTrue(result is ScanResult.Success)
        assertEquals(0, (result as ScanResult.Success).songs.size)
    }
}
