package com.doro.music.vm

import androidx.paging.PagingData
import com.doro.music.data.model.SortMode
import com.doro.music.data.repo.PlaylistRepo
import com.doro.music.domain.PlaybackUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockPlaylistRepo = mockk<PlaylistRepo>()
    private val mockPlaybackUseCase = mockk<PlaybackUseCase>(relaxed = true)

    private lateinit var viewModel: PlaylistsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockPlaylistRepo.getPlaylists(any()) } returns flowOf(PagingData.empty())
        coEvery { mockPlaylistRepo.getPlaylistCount() } returns flowOf(0)
        viewModel = PlaylistsViewModel(mockPlaylistRepo, mockPlaybackUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sortMode is LIST initially from BaseViewModel`() = runTest {
        viewModel.setSortBy(SortMode.TITLE)
        assertEquals(SortMode.TITLE, viewModel.sortMode.value)
    }

    @Test
    fun `showCreatePlaylist emits DialogStateCreate`() = runTest {
        viewModel.showCreatePlaylist()
        assertTrue(viewModel.dialogState.value is PlaylistsViewModel.DialogState.Create)
    }

    @Test
    fun `showDeletePlaylist emits DialogStateDelete`() = runTest {
        viewModel.showDeletePlaylist(123L)
        val state = viewModel.dialogState.value
        assertTrue(state is PlaylistsViewModel.DialogState.Delete)
        assertEquals(123L, (state as PlaylistsViewModel.DialogState.Delete).playlistId)
    }

    @Test
    fun `dismissDialog emits null`() = runTest {
        viewModel.showCreatePlaylist()
        assertNotNull(viewModel.dialogState.value)

        viewModel.dismissDialog()
        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `addToNext dispatches InsertGroup action`() = runTest {
        viewModel.addToNext(100L)

        verify { mockPlaybackUseCase.addGroupToNext(match { it is PlayContext.Playlist }) }
    }

    @Test
    fun `setSortBy updates sortMode`() = runTest {
        viewModel.setSortBy(SortMode.ARTIST)
        assertEquals(SortMode.ARTIST, viewModel.sortMode.value)
    }

    @Test
    fun `createPlaylist calls repo and dismisses dialog`() = runTest {
        coEvery { mockPlaylistRepo.createPlaylist("MyPlaylist") } returns true

        viewModel.showCreatePlaylist()
        assertNotNull(viewModel.dialogState.value)

        viewModel.createPlaylist("MyPlaylist")
        advanceUntilIdle()

        coVerify { mockPlaylistRepo.createPlaylist("MyPlaylist") }
        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `createPlaylist with failure still dismisses dialog`() = runTest {
        coEvery { mockPlaylistRepo.createPlaylist(any()) } returns false

        viewModel.showCreatePlaylist()
        viewModel.createPlaylist("BadPlaylist")
        advanceUntilIdle()

        coVerify { mockPlaylistRepo.createPlaylist("BadPlaylist") }
        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `createPlaylist with exception still dismisses dialog`() = runTest {
        coEvery { mockPlaylistRepo.createPlaylist(any()) } throws RuntimeException("db error")

        viewModel.showCreatePlaylist()
        viewModel.createPlaylist("ErrorPlaylist")
        advanceUntilIdle()

        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `deletePlaylist calls repo and dismisses dialog`() = runTest {
        coEvery { mockPlaylistRepo.deletePlaylist(42L) } returns true

        viewModel.showDeletePlaylist(42L)
        assertNotNull(viewModel.dialogState.value)

        viewModel.deletePlaylist(42L)
        advanceUntilIdle()

        coVerify { mockPlaylistRepo.deletePlaylist(42L) }
        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `deletePlaylist with failure still dismisses dialog`() = runTest {
        coEvery { mockPlaylistRepo.deletePlaylist(any()) } returns false

        viewModel.showDeletePlaylist(99L)
        viewModel.deletePlaylist(99L)
        advanceUntilIdle()

        coVerify { mockPlaylistRepo.deletePlaylist(99L) }
        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `deletePlaylist with exception still dismisses dialog`() = runTest {
        coEvery { mockPlaylistRepo.deletePlaylist(any()) } throws RuntimeException("db error")

        viewModel.showDeletePlaylist(1L)
        viewModel.deletePlaylist(1L)
        advanceUntilIdle()

        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `checkDuplicateName delegates to repo`() = runTest {
        coEvery { mockPlaylistRepo.isPlaylistNameExists("Existing") } returns true

        val result = viewModel.checkDuplicateName("Existing")

        assertTrue(result)
        coVerify { mockPlaylistRepo.isPlaylistNameExists("Existing") }
    }

    @Test
    fun `checkDuplicateName returns false for new name`() = runTest {
        coEvery { mockPlaylistRepo.isPlaylistNameExists("NewPlaylist") } returns false

        val result = viewModel.checkDuplicateName("NewPlaylist")

        assertFalse(result)
    }

    @Test
    fun `default sort mode is DATE_ADDED`() = runTest {
        assertEquals(SortMode.DATE_ADDED, viewModel.sortMode.value)
    }
}
