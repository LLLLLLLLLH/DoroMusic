package com.doro.music.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.doro.music.data.model.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.playerDataStore by preferencesDataStore(name = "player_state")

class PlayerStateDataStore(val context: Context){

    private val json = Json { ignoreUnknownKeys = true }

    val playerState: Flow<PlayerState?> = context.playerDataStore.data.map { preferences ->
        val jsonString = preferences[PLAYER_STATE]
        jsonString?.runCatching { json.decodeFromString<PlayerState>(this) }?.getOrNull()
    }

    suspend fun saveState(state: PlayerState) {
        context.playerDataStore.edit { preferences ->
           preferences[PLAYER_STATE] = json.encodeToString(state)
        }
    }

    suspend fun clearState() {
        context.playerDataStore.edit { preferences ->
            preferences.remove(PLAYER_STATE)
        }
    }

    private companion object{
        val PLAYER_STATE = stringPreferencesKey("player_state")
    }
}
