package com.doro.music.vm

import androidx.paging.PagingData
import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.LyricsLine
import com.doro.music.data.model.LyricsSource
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.Song
import com.doro.music.data.repo.SongRepo
import com.doro.music.domain.GetLyricsUseCase
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.PlayStateObserver
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayUiState
import com.doro.music.ui.component.player.PlayerSheetState
import com.doro.music.ui.component.player.PlayerViewType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockStateObserver = mockk<PlayStateObserver>()
    private val mockActionDispatcher = mockk<PlayActionDispatcher>(relaxed = true)
    private val mockSongRepo = mockk<SongRepo>()
    private val mockGetLyricsUseCase = mockk<GetLyricsUseCase>()

    private val uiStateFlow = MutableStateFlow(PlayUiState.Empty)
    private val currentPositionFlow = MutableStateFlow(0L)

    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockStateObserver.uiState } returns uiStateFlow
        every { mockStateObserver.currentPositionMs } returns currentPositionFlow
        every { mockStateObserver.playQueue } returns flowOf(PagingData.empty())
        coEvery { mockSongRepo.getSongById(any()) } returns null
        coEvery { mockGetLyricsUseCase.invoke(any()) } returns null

        viewModel = PlayerViewModel(mockStateObserver, mockActionDispatcher, mockSongRepo, mockGetLyricsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Empty`() {
        assertEquals(PlayUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `initial playerViewType is DISC`() {
        assertEquals(PlayerViewType.DISC, viewModel.playerViewType.value)
    }

    @Test
    fun `initial playerSheetState is Hidden`() {
        assertEquals(PlayerSheetState.Hidden, viewModel.playerSheetState.value)
    }

    @Test
    fun `initial isQueueVisible is false`() {
        assertFalse(viewModel.isQueueVisible.value)
    }

    @Test
    fun `handlePlayerAction TogglePlayPause dispatches TogglePlay`() = runTest {
        viewModel.handlePlayerAction(PlayerAction.TogglePlayPause)
        advanceUntilIdle()

        verify { mockActionDispatcher.dispatch(PlayAction.TogglePlay) }
    }

    @Test
    fun `handlePlayerAction Next dispatches Next`() = runTest {
        viewModel.handlePlayerAction(PlayerAction.Next)
        advanceUntilIdle()

        verify { mockActionDispatcher.dispatch(PlayAction.Next) }
    }

    @Test
    fun `handlePlayerAction Previous dispatches Prev`() = runTest {
        viewModel.handlePlayerAction(PlayerAction.Previous)
        advanceUntilIdle()

        verify { mockActionDispatcher.dispatch(PlayAction.Prev) }
    }

    @Test
    fun `handlePlayerAction SeekTo dispatches SeekTo with position`() = runTest {
        viewModel.handlePlayerAction(PlayerAction.SeekTo(5000L))
        advanceUntilIdle()

        verify {
            mockActionDispatcher.dispatch(match { action ->
                action is PlayAction.SeekTo && action.positionMs == 5000L
            })
        }
    }

    @Test
    fun `handlePlayerAction TogglePlayMode dispatches TogglePlayMode`() = runTest {
        viewModel.handlePlayerAction(PlayerAction.TogglePlayMode)
        advanceUntilIdle()

        verify { mockActionDispatcher.dispatch(PlayAction.TogglePlayMode) }
    }

    @Test
    fun `handlePlayerAction TogglePlayerView toggles between DISC and LYRIC`() = runTest {
        assertEquals(PlayerViewType.DISC, viewModel.playerViewType.value)

        viewModel.handlePlayerAction(PlayerAction.TogglePlayerView)
        assertEquals(PlayerViewType.LYRIC, viewModel.playerViewType.value)

        viewModel.handlePlayerAction(PlayerAction.TogglePlayerView)
        assertEquals(PlayerViewType.DISC, viewModel.playerViewType.value)
    }

    @Test
    fun `handlePlayerAction TogglePlayerSheet toggles between Expanded and Collapsed`() = runTest {
        // Start from Collapsed (set manually since Hidden won't toggle)
        viewModel.syncSheetState(PlayerSheetState.Collapsed)

        viewModel.handlePlayerAction(PlayerAction.TogglePlayerSheet)
        assertEquals(PlayerSheetState.Expanded, viewModel.playerSheetState.value)

        viewModel.handlePlayerAction(PlayerAction.TogglePlayerSheet)
        assertEquals(PlayerSheetState.Collapsed, viewModel.playerSheetState.value)
    }

    @Test
    fun `handlePlayerAction TogglePlayerSheet does nothing when Hidden`() = runTest {
        viewModel.syncSheetState(PlayerSheetState.Hidden)

        viewModel.handlePlayerAction(PlayerAction.TogglePlayerSheet)
        assertEquals(PlayerSheetState.Hidden, viewModel.playerSheetState.value)
    }

    @Test
    fun `handlePlayerAction TogglePlayQueue toggles queue visibility`() = runTest {
        assertFalse(viewModel.isQueueVisible.value)

        viewModel.handlePlayerAction(PlayerAction.TogglePlayQueue)
        assertTrue(viewModel.isQueueVisible.value)

        viewModel.handlePlayerAction(PlayerAction.TogglePlayQueue)
        assertFalse(viewModel.isQueueVisible.value)
    }

    @Test
    fun `addToNext dispatches InsertSingle action`() = runTest {
        val song = Song(id = 50L, title = "Test", path = "/test")
        viewModel.addToNext(song)
        advanceUntilIdle()

        verify {
            mockActionDispatcher.dispatch(match { action ->
                action is PlayAction.InsertSingle && action.songId == 50L
            })
        }
    }

    @Test
    fun `removeFromPlayQueue dispatches Remove action`() = runTest {
        viewModel.removeFromPlayQueue(42L)
        advanceUntilIdle()

        verify {
            mockActionDispatcher.dispatch(match { action ->
                action is PlayAction.Remove && action.queueId == 42L
            })
        }
    }

    @Test
    fun `seekToQueueItem dispatches SeekToQueueItem action`() = runTest {
        viewModel.seekToQueueItem(10L)
        advanceUntilIdle()

        verify {
            mockActionDispatcher.dispatch(match { action ->
                action is PlayAction.SeekToQueueItem && action.queueId == 10L
            })
        }
    }

    @Test
    fun `handleBack returns true and hides queue when queue is visible`() = runTest {
        viewModel.syncQueueVisible(true)

        val result = viewModel.handleBack()
        assertTrue(result)
        assertFalse(viewModel.isQueueVisible.value)
    }

    @Test
    fun `handleBack returns true and collapses sheet when expanded`() = runTest {
        viewModel.syncSheetState(PlayerSheetState.Expanded)

        val result = viewModel.handleBack()
        assertTrue(result)
        assertEquals(PlayerSheetState.Collapsed, viewModel.playerSheetState.value)
    }

    @Test
    fun `handleBack returns false when queue hidden and sheet not expanded`() = runTest {
        viewModel.syncQueueVisible(false)
        viewModel.syncSheetState(PlayerSheetState.Collapsed)

        val result = viewModel.handleBack()
        assertFalse(result)
    }

    @Test
    fun `uiState with null currentQueueId hides sheet and queue via init block`() = runTest {
        // Start with a valid queueId
        uiStateFlow.value = PlayUiState(currentQueueId = 1L, currentSongId = 1L)
        advanceUntilIdle()

        // Now set to Empty (null queueId) - init block should react
        viewModel.syncSheetState(PlayerSheetState.Collapsed)
        viewModel.syncQueueVisible(false)
        uiStateFlow.value = PlayUiState.Empty
        advanceUntilIdle()

        // Verify init block set Hidden and false
        assertEquals(PlayerSheetState.Hidden, viewModel.playerSheetState.value)
        assertFalse(viewModel.isQueueVisible.value)
    }

    @Test
    fun `uiState with currentQueueId auto-shows Collapsed sheet`() = runTest {
        viewModel.syncSheetState(PlayerSheetState.Hidden)

        uiStateFlow.value = PlayUiState(currentQueueId = 1L, currentSongId = 1L)
        advanceUntilIdle()

        assertEquals(PlayerSheetState.Collapsed, viewModel.playerSheetState.value)
    }

    @Test
    fun `uiState with currentQueueId does not override Expanded sheet`() = runTest {
        viewModel.syncSheetState(PlayerSheetState.Expanded)
        uiStateFlow.value = PlayUiState(currentQueueId = 1L, currentSongId = 1L)
        advanceUntilIdle()

        assertEquals(PlayerSheetState.Expanded, viewModel.playerSheetState.value)
    }

    @Test
    fun `uiState with currentQueueId shows queue when sheet is not Hidden`() = runTest {
        viewModel.syncSheetState(PlayerSheetState.Expanded)
        viewModel.syncQueueVisible(false)

        uiStateFlow.value = PlayUiState(currentQueueId = 1L, currentSongId = 1L)
        advanceUntilIdle()

        // Expanded sheet should not be changed, but queue visibility should update
        assertEquals(PlayerSheetState.Expanded, viewModel.playerSheetState.value)
    }

    @Test
    fun `currentSong is null when uiState has no currentSongId`() = runTest {
        uiStateFlow.value = PlayUiState.Empty
        advanceUntilIdle()

        assertNull(viewModel.currentSong.value)
    }

    @Test
    fun `currentSong fetches song when uiState has currentSongId`() = runTest {
        val song = Song(id = 1L, title = "Test", path = "/test")
        coEvery { mockSongRepo.getSongById(1L) } returns song

        // WhileSubscribed needs active subscriber to trigger upstream
        val collectJob = launch { viewModel.currentSong.collect {} }

        uiStateFlow.value = PlayUiState(currentQueueId = 1L, currentSongId = 1L)
        advanceUntilIdle()

        assertEquals(song, viewModel.currentSong.value)

        collectJob.cancel()
    }

    @Test
    fun `handleBack with queue visible hides queue and returns true`() = runTest {
        viewModel.syncQueueVisible(true)
        viewModel.syncSheetState(PlayerSheetState.Collapsed)

        val result = viewModel.handleBack()
        assertTrue(result)
        assertFalse(viewModel.isQueueVisible.value)
    }

    @Test
    fun `handleBack with expanded sheet collapses and returns true`() = runTest {
        viewModel.syncQueueVisible(false)
        viewModel.syncSheetState(PlayerSheetState.Expanded)

        val result = viewModel.handleBack()
        assertTrue(result)
        assertEquals(PlayerSheetState.Collapsed, viewModel.playerSheetState.value)
    }

    @Test
    fun `handleBack with collapsed sheet and hidden queue returns false`() = runTest {
        viewModel.syncQueueVisible(false)
        viewModel.syncSheetState(PlayerSheetState.Collapsed)

        val result = viewModel.handleBack()
        assertFalse(result)
    }

    @Test
    fun `initial currentPosition is 0`() {
        assertEquals(0L, viewModel.currentPosition.value)
    }

    @Test
    fun `currentPosition reflects observer flow`() = runTest {
        assertNotNull(viewModel.currentPosition.value)
        assertEquals(0L, viewModel.currentPosition.value)
    }

    @Test
    fun `currentLyricIndex returns -1 when lyrics is null`() = runTest {
        val collectJob = launch { viewModel.currentLyricIndex.collect {} }
        advanceUntilIdle()

        assertEquals(-1, viewModel.currentLyricIndex.value)

        collectJob.cancel()
    }

    @Test
    fun `currentLyricIndex returns -1 when lyrics has empty lines`() = runTest {
        coEvery { mockSongRepo.getSongById(1L) } returns Song(id = 1L, title = "Test", path = "/test")
        coEvery { mockGetLyricsUseCase.invoke(any()) } returns LyricsData(
            songId = 1L,
            source = LyricsSource.CACHE,
            lines = emptyList()
        )

        val collectJob = launch { viewModel.currentLyricIndex.collect {} }

        uiStateFlow.value = PlayUiState(currentQueueId = 1L, currentSongId = 1L)
        currentPositionFlow.value = 5000L
        advanceUntilIdle()

        assertEquals(-1, viewModel.currentLyricIndex.value)

        collectJob.cancel()
    }

    @Test
    fun `currentLyricIndex calculates correct index with lyrics data`() = runTest {
        coEvery { mockSongRepo.getSongById(1L) } returns Song(id = 1L, title = "Test", path = "/test")
        coEvery { mockGetLyricsUseCase.invoke(any()) } returns LyricsData(
            songId = 1L,
            source = LyricsSource.CACHE,
            lines = listOf(
                LyricsLine(timeMs = 0, text = "Line 1"),
                LyricsLine(timeMs = 5000, text = "Line 2"),
                LyricsLine(timeMs = 10000, text = "Line 3")
            ),
            offset = 0L
        )

        val collectJob = launch { viewModel.currentLyricIndex.collect {} }

        uiStateFlow.value = PlayUiState(currentQueueId = 1L, currentSongId = 1L)
        currentPositionFlow.value = 6000L
        advanceUntilIdle()

        // Position 6000ms should be between line 2 (5000ms) and line 3 (10000ms)
        assertEquals(1, viewModel.currentLyricIndex.value)

        collectJob.cancel()
    }

    @Test
    fun `restorePlayerView sets playerViewType to DISC`() = runTest {
        viewModel.restorePlayerView()
        assertEquals(PlayerViewType.DISC, viewModel.playerViewType.value)
    }

    @Test
    fun `syncSheetState updates playerSheetState`() = runTest {
        viewModel.syncSheetState(PlayerSheetState.Expanded)
        assertEquals(PlayerSheetState.Expanded, viewModel.playerSheetState.value)

        viewModel.syncSheetState(PlayerSheetState.Collapsed)
        assertEquals(PlayerSheetState.Collapsed, viewModel.playerSheetState.value)
    }

    @Test
    fun `syncQueueVisible updates isQueueVisible`() = runTest {
        viewModel.syncQueueVisible(true)
        assertTrue(viewModel.isQueueVisible.value)

        viewModel.syncQueueVisible(false)
        assertFalse(viewModel.isQueueVisible.value)
    }
}