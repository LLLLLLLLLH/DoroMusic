package com.doro.music.domain

import com.doro.music.data.model.Song
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayContext

class PlaybackUseCase(
    private val actionDispatcher: PlayActionDispatcher
) {

    fun play(song: Song?, playContext: PlayContext) {
        val targetSong = song ?: return
        actionDispatcher.dispatch(PlayAction.Play(targetSong.id, playContext))
    }

    fun playFirst(songs: List<Song>, playContext: PlayContext) {
        play(songs.firstOrNull(), playContext)
    }

    fun shufflePlay(songs: List<Song>, playContext: PlayContext) {
        play(songs.randomOrNull(), playContext)
    }

    fun addToNext(song: Song) {
        actionDispatcher.dispatch(PlayAction.InsertSingle(song.id))
    }

    fun addGroupToNext(playContext: PlayContext) {
        actionDispatcher.dispatch(PlayAction.InsertGroup(playContext))
    }
}
