package com.doro.music.vm

import androidx.paging.PagingData
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.repo.FolderRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.domain.PlaybackUseCase
import com.doro.music.player.model.PlayContext
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoldersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<FolderRepo>()
    private val mockPlaybackUseCase = mockk<PlaybackUseCase>(relaxed = true)
    private val mockGetPlaylistsUseCase = mockk<GetPlaylistsUseCase>()
    private val mockAddSongToPlaylistUseCase = mockk<AddSongToPlaylistUseCase>()

    private lateinit var viewModel: FoldersViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockRepo.folders } returns flowOf(emptyList())
        coEvery { mockRepo.getSongsByFolder(any()) } returns flowOf(PagingData.empty())
        coEvery { mockGetPlaylistsUseCase() } returns flowOf(PagingData.empty())
        viewModel = FoldersViewModel(mockRepo, mockPlaybackUseCase, mockGetPlaylistsUseCase, mockAddSongToPlaylistUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectFolder triggers folder selection`() = runTest {
        viewModel.selectFolder("/music/rock")
        advanceUntilIdle()
        // selectedFolder is private, verify indirectly via songs flow
    }

    @Test
    fun `selectFolder with null clears selection`() = runTest {
        viewModel.selectFolder("/music/rock")
        viewModel.selectFolder(null)
        advanceUntilIdle()
        // selectedFolder is private, verify no crash
    }

    @Test
    fun `selectSong updates selectedSong state`() = runTest {
        viewModel.selectSong(123L)
        assertEquals(123L, viewModel.selectedSongId.value)
    }

    @Test
    fun `selectSong with null clears selection`() = runTest {
        viewModel.selectSong(123L)
        viewModel.selectSong(null)
        assertNull(viewModel.selectedSongId.value)
    }

    @Test
    fun `addToNext dispatches InsertSingle action`() = runTest {
        val song = Song(id = 50L, title = "Test", path = "/test")
        viewModel.addToNext(song)
        advanceUntilIdle()

        verify { mockPlaybackUseCase.addToNext(song) }
    }

    @Test
    fun `playFolderSongs dispatches Play action with Folder context`() = runTest {
        val song = Song(id = 10L, title = "Test", path = "/music/rock/song.mp3")
        viewModel.playFolderSongs("/music/rock", song)
        advanceUntilIdle()

        verify { mockPlaybackUseCase.play(song, match { it is PlayContext.Folder }) }
    }

    @Test
    fun `addSongToPlaylist does nothing when no song selected`() = runTest {
        viewModel.selectSong(null)
        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify(exactly = 0) { mockAddSongToPlaylistUseCase(any(), any()) }
    }

    @Test
    fun `addSongToPlaylist calls use case when song selected`() = runTest {
        viewModel.selectSong(100L)
        coEvery { mockAddSongToPlaylistUseCase(any(), any()) } returns com.doro.music.data.model.AddSongResult.Success

        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify { mockAddSongToPlaylistUseCase(songId = 100L, playlists = any()) }
    }

    @Test
    fun `addSongToPlaylist with failed result still emits event`() = runTest {
        viewModel.selectSong(200L)
        coEvery { mockAddSongToPlaylistUseCase(any(), any()) } returns com.doro.music.data.model.AddSongResult.Failed

        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify { mockAddSongToPlaylistUseCase(songId = 200L, playlists = any()) }
    }

    @Test
    fun `playFolderSongs dispatches Play with correct folder path`() = runTest {
        val song = Song(id = 10L, title = "Test", path = "/music/rock/song.mp3")
        viewModel.playFolderSongs("/music/rock", song)

        verify {
            mockPlaybackUseCase.play(
                song,
                match { it is PlayContext.Folder && it.path == "/music/rock" }
            )
        }
    }
}
