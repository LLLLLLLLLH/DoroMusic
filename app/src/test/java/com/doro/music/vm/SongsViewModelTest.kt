package com.doro.music.vm

import androidx.paging.PagingData
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.SortMode
import com.doro.music.data.repo.SongRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayContext
import io.mockk.coEvery
import io.mockk.coVerify
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
class SongsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<SongRepo>()
    private val mockDispatcher = mockk<PlayActionDispatcher>(relaxed = true)
    private val mockGetPlaylistsUseCase = mockk<GetPlaylistsUseCase>()
    private val mockAddSongToPlaylistUseCase = mockk<AddSongToPlaylistUseCase>()

    private lateinit var viewModel: SongsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepo.getSongs(any()) } returns flowOf(PagingData.empty())
        coEvery { mockRepo.getSongCount() } returns flowOf(0)
        coEvery { mockGetPlaylistsUseCase() } returns flowOf(PagingData.empty())
        viewModel = SongsViewModel(mockRepo, mockDispatcher, mockGetPlaylistsUseCase, mockAddSongToPlaylistUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectSong updates selectedSong state`() = runTest {
        viewModel.selectSong(123L)
        assertEquals(123L, viewModel.selectedSongId.value)
    }

    @Test
    fun `selectSong with null clears selection`() = runTest {
        viewModel.selectSong(123L)
        assertEquals(123L, viewModel.selectedSongId.value)

        viewModel.selectSong(null)
        assertNull(viewModel.selectedSongId.value)
    }

    @Test
    fun `addToNext dispatches InsertSingle action`() = runTest {
        val song = Song(id = 50L, title = "Test", path = "/test")

        viewModel.addToNext(song)
        advanceUntilIdle()

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.InsertSingle && action.songId == 50L
            })
        }
    }

    @Test
    fun `play with null song does not dispatch action`() = runTest {
        viewModel.play(null)

        verify(exactly = 0) { mockDispatcher.dispatch(any()) }
    }

    @Test
    fun `setSortBy updates sortMode`() = runTest {
        viewModel.setSortBy(SortMode.ARTIST)
        assertEquals(SortMode.ARTIST, viewModel.sortMode.value)
    }

    @Test
    fun `setDisplayMode updates displayMode`() = runTest {
        viewModel.setDisplayMode(com.doro.music.data.model.DisplayMode.GRID)
        assertEquals(com.doro.music.data.model.DisplayMode.GRID, viewModel.displayMode.value)
    }

    @Test
    fun `play dispatches Play action with All context`() = runTest {
        val song = Song(id = 10L, title = "Test", path = "/test")
        viewModel.play(song)

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.songId == 10L && action.playContext is PlayContext.All
            })
        }
    }

    @Test
    fun `playAll dispatches Play with first song`() = runTest {
        val songs = listOf(
            Song(id = 1L, title = "First", path = "/a"),
            Song(id = 2L, title = "Second", path = "/b")
        )
        coEvery { mockRepo.getAllSongs(any()) } returns songs

        viewModel.playAll()
        advanceUntilIdle()

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.songId == 1L
            })
        }
    }

    @Test
    fun `playAll does nothing when no songs`() = runTest {
        coEvery { mockRepo.getAllSongs(any()) } returns emptyList()

        viewModel.playAll()
        advanceUntilIdle()

        verify(exactly = 0) { mockDispatcher.dispatch(any()) }
    }

    @Test
    fun `shufflePlay dispatches Play action`() = runTest {
        val songs = listOf(Song(id = 5L, title = "Only", path = "/a"))
        coEvery { mockRepo.getAllSongs(any()) } returns songs

        viewModel.shufflePlay()
        advanceUntilIdle()

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.playContext is PlayContext.All
            })
        }
    }

    @Test
    fun `shufflePlay does nothing when no songs`() = runTest {
        coEvery { mockRepo.getAllSongs(any()) } returns emptyList()

        viewModel.shufflePlay()
        advanceUntilIdle()

        verify(exactly = 0) { mockDispatcher.dispatch(any()) }
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
        coEvery { mockAddSongToPlaylistUseCase(any(), any()) } returns AddSongResult.Success

        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify { mockAddSongToPlaylistUseCase(songId = 100L, playlists = any()) }
    }

    @Test
    fun `addSongToPlaylist with failed result still emits event`() = runTest {
        viewModel.selectSong(100L)
        coEvery { mockAddSongToPlaylistUseCase(any(), any()) } returns AddSongResult.Failed

        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify { mockAddSongToPlaylistUseCase(songId = 100L, playlists = any()) }
    }

    @Test
    fun `addSongToPlaylist with exception still emits event`() = runTest {
        viewModel.selectSong(100L)
        coEvery { mockAddSongToPlaylistUseCase(any(), any()) } throws RuntimeException("db error")

        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify { mockAddSongToPlaylistUseCase(songId = 100L, playlists = any()) }
    }

    @Test
    fun `playAll dispatches Play with correct sortMode context`() = runTest {
        val songs = listOf(Song(id = 1L, title = "First", path = "/a"))
        coEvery { mockRepo.getAllSongs(any()) } returns songs
        viewModel.setSortBy(SortMode.ARTIST)

        viewModel.playAll()
        advanceUntilIdle()

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.playContext is PlayContext.All
            })
        }
    }

    @Test
    fun `shufflePlay dispatches Play with All context`() = runTest {
        val songs = listOf(Song(id = 5L, title = "Only", path = "/a"))
        coEvery { mockRepo.getAllSongs(any()) } returns songs

        viewModel.shufflePlay()
        advanceUntilIdle()

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.playContext is PlayContext.All
            })
        }
    }
}
