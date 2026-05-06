package com.doro.music.domain

import com.doro.music.data.model.Song
import com.doro.music.data.repo.PlaybackRepository

class PlaySongsUseCase(private val playbackRepository: PlaybackRepository) {
    suspend operator fun invoke(songs: List<Song>, song: Song? = null, randomize: Boolean = false) {
        if (songs.isEmpty()) return
        val startIndex = when {
            song != null -> songs.indexOfSong(song)
            randomize -> songs.indices.random()
            else -> 0
        }
        playbackRepository.playSongs(songs, startIndex)
    }
}

private fun List<Song>.indexOfSong(song: Song?): Int {
    return if (song == null) 0 else indexOfFirst { it.id == song.id }.coerceAtLeast(0)
}
