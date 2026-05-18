package com.doro.music.domain

import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.repo.PlaylistRepo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AddSongToPlaylistUseCaseTest {

    private val mockRepo = mockk<PlaylistRepo>()
    private val useCase = AddSongToPlaylistUseCase(mockRepo)

    @Test
    fun `invoke delegates to repo addSongToPlaylist and returns Success`() = runTest {
        val playlists = listOf(
            Playlist(id = 1L, name = "Playlist 1"),
            Playlist(id = 2L, name = "Playlist 2")
        )
        val songId = 100L
        coEvery { mockRepo.addSongToPlaylist(playlists, songId) } returns AddSongResult.Success

        val result = useCase.invoke(playlists, songId)

        assertEquals(AddSongResult.Success, result)
    }

    @Test
    fun `invoke returns AlreadyExists when song already in playlist`() = runTest {
        val playlists = listOf(Playlist(id = 1L, name = "My Playlist"))
        val songId = 50L
        coEvery { mockRepo.addSongToPlaylist(playlists, songId) } returns AddSongResult.AlreadyExists

        val result = useCase.invoke(playlists, songId)

        assertEquals(AddSongResult.AlreadyExists, result)
    }

    @Test
    fun `invoke returns Failed when operation fails`() = runTest {
        val playlists = listOf(Playlist(id = 1L, name = "Test"))
        val songId = 25L
        coEvery { mockRepo.addSongToPlaylist(playlists, songId) } returns AddSongResult.Failed

        val result = useCase.invoke(playlists, songId)

        assertEquals(AddSongResult.Failed, result)
    }
}
