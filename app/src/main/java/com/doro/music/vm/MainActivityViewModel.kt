package com.doro.music.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doro.music.data.datastore.SettingsDataStore
import com.doro.music.data.model.AppSettings
import com.doro.music.data.model.DarkThemeMode
import com.doro.music.player.PlayerConnector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    settingsDataStore: SettingsDataStore,
    private val connector: PlayerConnector
) : ViewModel() {

    fun connect() = connector.connect()

    fun disconnect() = connector.disconnect()

    val darkThemeMode: StateFlow<DarkThemeMode> = settingsDataStore.settings
        .map { it.darkTheme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings().darkTheme
        )
}
