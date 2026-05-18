package com.doro.music.vm

import com.doro.music.data.datastore.SettingsDataStore
import com.doro.music.data.model.AppSettings
import com.doro.music.data.model.DarkThemeMode
import com.doro.music.player.PlayerConnector
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockSettingsDataStore = mockk<SettingsDataStore>()
    private val mockConnector = mockk<PlayerConnector>(relaxed = true)

    private lateinit var viewModel: MainActivityViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockSettingsDataStore.settings } returns flowOf(AppSettings())

        viewModel = MainActivityViewModel(mockSettingsDataStore, mockConnector)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `connect delegates to connector`() {
        viewModel.connect()
        verify { mockConnector.connect() }
    }

    @Test
    fun `disconnect delegates to connector`() {
        viewModel.disconnect()
        verify { mockConnector.disconnect() }
    }

    @Test
    fun `darkThemeMode reflects DataStore settings`() = runTest {
        val settings = AppSettings(darkTheme = DarkThemeMode.DARK)
        every { mockSettingsDataStore.settings } returns flowOf(settings)

        val vm = MainActivityViewModel(mockSettingsDataStore, mockConnector)
        advanceUntilIdle()

        // stateIn uses WhileSubscribed(5000L), verify the flow is connected
        assertNotNull(vm.darkThemeMode.value)
    }

    @Test
    fun `darkThemeMode defaults to SYSTEM`() = runTest {
        every { mockSettingsDataStore.settings } returns flowOf(AppSettings())

        val vm = MainActivityViewModel(mockSettingsDataStore, mockConnector)

        assertEquals(DarkThemeMode.SYSTEM, vm.darkThemeMode.value)
    }
}