package com.doro.music.vm

import androidx.paging.PagingData
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.SortMode
import com.doro.music.data.repo.SearchRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.model.PlayAction
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<SearchRepo>()
    private val mockDispatcher = mockk<PlayActionDispatcher>(relaxed = true)
    private val mockGetPlaylistsUseCase = mockk<GetPlaylistsUseCase>()
    private val mockAddSongToPlaylistUseCase = mockk<AddSongToPlaylistUseCase>()

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepo.getSongsByKeyWords(any(), any()) } returns flowOf(PagingData.empty())
        coEvery { mockRepo.getSongCountByKeyWords(any()) } returns flowOf(0)
        coEvery { mockGetPlaylistsUseCase() } returns flowOf(PagingData.empty())
        viewModel = SearchViewModel(mockRepo, mockDispatcher, mockGetPlaylistsUseCase, mockAddSongToPlaylistUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchSongs updates keyword state`() = runTest {
        viewModel.searchSongs("test query")
        assertEquals("test query", viewModel.keyword.value)
    }

    @Test
    fun `selectSong updates selectedSong state`() = runTest {
        viewModel.selectSong(123L)
        assertEquals(123L, viewModel.selectedSong.value)
    }

    @Test
    fun `addToNext dispatches InsertSingle action`() = runTest {
        val song = Song(id = 50L, title = "Test", path = "/test")
        viewModel.addToNext(song)

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
    fun `play dispatches Play action with Search context`() = runTest {
        val song = Song(id = 10L, title = "Test", path = "/test")
        viewModel.searchSongs("test query")

        viewModel.play(song)

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.songId == 10L && action.playContext is PlayContext.Search
            })
        }
    }

    @Test
    fun `playAll dispatches Play with first song`() = runTest {
        viewModel.searchSongs("test")
        val songs = listOf(
            Song(id = 1L, title = "First", path = "/a"),
            Song(id = 2L, title = "Second", path = "/b")
        )
        every { mockRepo.getAllSongsByKeyWords(any(), any()) } returns flowOf(songs)

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
        viewModel.searchSongs("empty")
        every { mockRepo.getAllSongsByKeyWords(any(), any()) } returns flowOf(emptyList())

        viewModel.playAll()
        advanceUntilIdle()

        verify(exactly = 0) { mockDispatcher.dispatch(any()) }
    }

    @Test
    fun `shufflePlay dispatches Play action`() = runTest {
        viewModel.searchSongs("test")
        val songs = listOf(Song(id = 5L, title = "Only", path = "/a"))
        every { mockRepo.getAllSongsByKeyWords(any(), any()) } returns flowOf(songs)

        viewModel.shufflePlay()
        advanceUntilIdle()

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.playContext is PlayContext.Search
            })
        }
    }

    @Test
    fun `shufflePlay does nothing when no songs`() = runTest {
        viewModel.searchSongs("empty")
        every { mockRepo.getAllSongsByKeyWords(any(), any()) } returns flowOf(emptyList())

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
}
