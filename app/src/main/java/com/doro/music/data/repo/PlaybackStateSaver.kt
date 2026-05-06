package com.doro.music.data.repo

import android.util.Log
import com.doro.music.data.datastore.PlayerStateDataStore
import com.doro.music.data.model.PlayerState
import com.doro.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class PlaybackStateSaver(
    private val playQueueStore: PlayQueueStore,
    private val playerStateDataStore: PlayerStateDataStore
) {

    private companion object {
        private const val TAG = "PlaybackStateSaver"
    }

    data class RestoredState(
        val songs: List<Song>,
        val currentIndex: Int,
        val currentPosition: Long
    )

    suspend fun save(songs: List<Song>, currentIndex: Int, currentPosition: Long) {
        saveByIds(songs.map { it.id }, currentIndex, currentPosition)
    }

    suspend fun saveByIds(songIds: List<Long>, currentIndex: Int, currentPosition: Long) {
        if (songIds.isEmpty()) return
        withContext(Dispatchers.IO) {
            try {
                playQueueStore.save(songIds)
                playerStateDataStore.saveState(
                    PlayerState(
                        currentIndex = currentIndex,
                        currentPosition = currentPosition
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save playback state", e)
            }
        }
    }

    suspend fun restore(): RestoredState? {
        return withContext(Dispatchers.IO) {
            try {
                val songs = playQueueStore.load()
                if (songs.isEmpty()) return@withContext null
                val savedState = playerStateDataStore.playerState.firstOrNull()
                val currentIndex = savedState?.currentIndex?.coerceIn(0, songs.lastIndex) ?: 0
                val currentPosition = savedState?.currentPosition?.coerceAtLeast(0L) ?: 0L
                RestoredState(songs, currentIndex, currentPosition)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore playback state", e)
                null
            }
        }
    }
}
