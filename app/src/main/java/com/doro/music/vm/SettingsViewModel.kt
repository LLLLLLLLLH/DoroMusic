package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import com.doro.music.base.BaseViewModel
import com.doro.music.data.datastore.SettingsDataStore
import com.doro.music.data.model.AppSettings
import com.doro.music.data.model.DarkThemeMode
import com.doro.music.data.model.PlayMode
import com.doro.music.data.repo.FolderRepo
import com.doro.music.data.repo.SettingsRepo
import com.doro.music.domain.GetSongFoldersUseCase
import com.doro.music.domain.ScanMusicUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo : SettingsRepo,
    private val settingsDataStore: SettingsDataStore,
    private val getSongFoldersUseCase: GetSongFoldersUseCase,
    private val scanMusicUseCase: ScanMusicUseCase
) : BaseViewModel() {

    val settings = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AppSettings()
    )

    val folders = getSongFoldersUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun setMinDurationFilter(seconds: Int) {
        viewModelScope.launch { settingsDataStore.updateMinDurationFilter(seconds) }
    }

    fun setExcludedFolders(folders: List<String>) {
        viewModelScope.launch {
            repo.setExcludedFolders(folders)
            scanMusicUseCase()
        }
    }

    fun setPlayMode(mode: PlayMode) {
        viewModelScope.launch { settingsDataStore.updatePlayMode(mode) }
    }

    fun setDarkTheme(mode: DarkThemeMode) {
        viewModelScope.launch { settingsDataStore.updateDarkTheme(mode) }
    }
}
