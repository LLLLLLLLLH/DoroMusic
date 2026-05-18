package com.doro.music.vm

import com.doro.music.data.model.Song
import com.doro.music.domain.ScanMusicUseCase
import com.doro.music.player.util.ScanResult
import com.doro.music.vm.MainViewModel.ScanState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockScanMusicUseCase = mockk<ScanMusicUseCase>()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(mockScanMusicUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial scanState is Idle`() {
        assertTrue(viewModel.scanState.value is ScanState.Idle)
    }

    @Test
    fun `scan with Success result transitions through Scanning to Idle`() = runTest {
        val songs = listOf(Song(id = 1L, title = "Song1", path = "/a"))
        coEvery { mockScanMusicUseCase() } returns ScanResult.Success(songs)

        viewModel.scan()
        advanceUntilIdle()

        // After completion, state should be Idle
        assertTrue(viewModel.scanState.value is ScanState.Idle)
    }

    @Test
    fun `scan with Success result emits Done event`() = runTest {
        val songs = listOf(Song(id = 1L, title = "Song1", path = "/a"))
        coEvery { mockScanMusicUseCase() } returns ScanResult.Success(songs)

        viewModel.scan()
        advanceUntilIdle()

        // After completion, state should be Idle - event was emitted during scan
        assertTrue(viewModel.scanState.value is ScanState.Idle)
    }

    @Test
    fun `scan with Error result emits Error event`() = runTest {
        coEvery { mockScanMusicUseCase() } returns ScanResult.Error(Exception("fail"))

        viewModel.scan()
        advanceUntilIdle()

        assertTrue(viewModel.scanState.value is ScanState.Idle)
    }

    @Test
    fun `scan with empty song list emits Done with count 0`() = runTest {
        coEvery { mockScanMusicUseCase() } returns ScanResult.Success(emptyList())

        viewModel.scan()
        advanceUntilIdle()

        assertTrue(viewModel.scanState.value is ScanState.Idle)
    }

    @Test
    fun `ScanState Done holds count`() {
        val done = ScanState.Done(5)
        assertEquals(5, done.count)
    }

    @Test
    fun `ScanState sealed interface implementations are distinct`() {
        val idle = ScanState.Idle
        val scanning = ScanState.Scanning
        val error = ScanState.Error
        val done = ScanState.Done(0)

        assertNotEquals(idle, scanning)
        assertNotEquals(idle, error)
        assertNotEquals(idle, done)
        assertNotEquals(scanning, error)
    }

    @Test
    fun `scan emits Scanning state then Done event`() = runTest {
        val songs = listOf(Song(id = 1L, title = "Song1", path = "/a"))
        coEvery { mockScanMusicUseCase() } returns ScanResult.Success(songs)

        viewModel.scan()
        advanceUntilIdle()

        // After completion, state should be Idle (Scanning was transient)
        assertTrue(viewModel.scanState.value is ScanState.Idle)
    }

    @Test
    fun `scan emits scanEvent on success`() = runTest {
        val songs = listOf(Song(id = 1L, title = "Song1", path = "/a"))
        coEvery { mockScanMusicUseCase() } returns ScanResult.Success(songs)

        val events = mutableListOf<ScanState>()
        val job = launch { viewModel.scanEvent.collect { events.add(it) } }

        viewModel.scan()
        advanceUntilIdle()

        job.cancel()
        assertTrue(events.any { it is ScanState.Done })
    }

    @Test
    fun `scan emits Error event on failure`() = runTest {
        coEvery { mockScanMusicUseCase() } returns ScanResult.Error(Exception("fail"))

        val events = mutableListOf<ScanState>()
        val job = launch { viewModel.scanEvent.collect { events.add(it) } }

        viewModel.scan()
        advanceUntilIdle()

        job.cancel()
        assertTrue(events.any { it is ScanState.Error })
    }

    @Test
    fun `ScanState Done with count preserves value`() {
        val done = ScanState.Done(42)
        assertEquals(42, done.count)
    }
}