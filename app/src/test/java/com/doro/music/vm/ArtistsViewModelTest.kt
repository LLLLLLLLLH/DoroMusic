package com.doro.music.vm

import androidx.paging.PagingData
import com.doro.music.data.model.SortMode
import com.doro.music.data.repo.ArtistRepo
import com.doro.music.domain.PlaybackUseCase
import com.doro.music.player.model.PlayContext
import io.mockk.coEvery
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
class ArtistsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<ArtistRepo>()
    private val mockPlaybackUseCase = mockk<PlaybackUseCase>(relaxed = true)

    private lateinit var viewModel: ArtistsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepo.getArtists() } returns flowOf(PagingData.empty())
        coEvery { mockRepo.getArtistCount() } returns flowOf(0)
        viewModel = ArtistsViewModel(mockRepo, mockPlaybackUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default sort mode is TITLE from BaseViewModel`() {
        assertEquals(SortMode.TITLE, viewModel.sortMode.value)
    }

    @Test
    fun `setSortBy updates sortMode`() = runTest {
        viewModel.setSortBy(SortMode.ARTIST)
        assertEquals(SortMode.ARTIST, viewModel.sortMode.value)
    }

    @Test
    fun `addArtistToNext dispatches InsertGroup action`() = runTest {
        viewModel.addArtistToNext("TestArtist")
        advanceUntilIdle()

        verify { mockPlaybackUseCase.addGroupToNext(match { it is PlayContext.Artist }) }
    }

    @Test
    fun `setDisplayMode updates displayMode`() = runTest {
        viewModel.setDisplayMode(com.doro.music.data.model.DisplayMode.GRID)
        assertEquals(com.doro.music.data.model.DisplayMode.GRID, viewModel.displayMode.value)
    }
}
