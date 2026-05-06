package com.doro.music.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.doro.music.data.model.AppSettings
import com.doro.music.data.model.DarkThemeMode
import com.doro.music.data.model.PlayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsDataStore(val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { decodeSettings(it) }

    private fun decodeSettings(preferences: Preferences): AppSettings =
        preferences[APP_SETTINGS]
            ?.runCatching { json.decodeFromString<AppSettings>(this) }
            ?.getOrNull() ?: AppSettings()

    private suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { preferences ->
            val current = decodeSettings(preferences)
            preferences[APP_SETTINGS] = json.encodeToString(transform(current))
        }
    }

    suspend fun updatePlayMode(mode: PlayMode) = updateSettings { it.copy(defaultPlayMode = mode) }
    suspend fun updateDarkTheme(mode: DarkThemeMode) = updateSettings { it.copy(darkTheme = mode) }
    suspend fun updateMinDurationFilter(seconds: Int) = updateSettings { it.copy(minDurationFilter = seconds) }

    private companion object {
        val APP_SETTINGS = stringPreferencesKey("app_settings")
    }
}
