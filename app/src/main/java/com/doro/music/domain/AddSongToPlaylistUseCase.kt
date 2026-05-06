package com.doro.music.domain

import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.repo.PlaylistRepo

class AddSongToPlaylistUseCase(private val repo: PlaylistRepo) {

    suspend operator fun invoke(playlists: List<Playlist>, songId: Long): AddSongResult = repo.addSongToPlaylist(playlists, songId)
}
