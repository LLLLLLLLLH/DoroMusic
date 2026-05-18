package com.doro.music.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.doro.music.data.model.PlayMode
import com.doro.music.player.model.PersistedPlayState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.playStateDataStore by preferencesDataStore(name = "new_player_state")

class PlayStateDataStoreImpl(
    private val context: Context
) : PlayStateDataStore {

    private val store = context.playStateDataStore

    override val currentQueueId = store.data.map { it[KEY_CURRENT_QUEUE_ID] ?: 0L }
    override val currentSongId = store.data.map { it[KEY_CURRENT_SONG_ID] ?: 0L }
    override val currentPositionMs = store.data.map { it[KEY_POSITION_MS] ?: 0L }
    override val playMode = store.data.map { prefs -> PlayMode.pares(prefs[KEY_PLAY_MODE]) }
    override val shuffleSeed = store.data.map { it[KEY_SHUFFLE_SEED] ?: 0L }

    override val persistentState = combine(
        currentQueueId,
        currentSongId,
        currentPositionMs,
        playMode,
        shuffleSeed
    ) { queueId, songId, position, mode, seed ->
        PersistedPlayState(
            currentQueueId = queueId,
            currentSongId = songId,
            positionMs = position,
            playMode = mode,
            shuffleSeed = seed
        )
    }

    override suspend fun saveCurrentQueueId(queueId: Long) {
        store.edit { it[KEY_CURRENT_QUEUE_ID] = queueId }
    }

    override suspend fun saveCurrentSongId(songId: Long) {
        store.edit { it[KEY_CURRENT_SONG_ID] = songId }
    }

    override suspend fun saveCurrentPosition(positionMs: Long) {
        store.edit { it[KEY_POSITION_MS] = positionMs }
    }

    override suspend fun savePlayMode(mode: PlayMode) {
        store.edit { it[KEY_PLAY_MODE] = mode.name }
    }

    override suspend fun saveShuffleSeed(seed: Long) {
        store.edit { it[KEY_SHUFFLE_SEED] = seed }
    }

    override suspend fun clearState() {
        store.edit { it.clear() }
    }

    private companion object {
        val KEY_CURRENT_QUEUE_ID = longPreferencesKey("current_queue_id")
        val KEY_CURRENT_SONG_ID = longPreferencesKey("current_song_id")
        val KEY_POSITION_MS = longPreferencesKey("position_ms")
        val KEY_PLAY_MODE = stringPreferencesKey("play_mode")
        val KEY_SHUFFLE_SEED = longPreferencesKey("shuffle_seed")
    }
}
