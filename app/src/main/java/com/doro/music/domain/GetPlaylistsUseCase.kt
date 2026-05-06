package com.doro.music.domain

import androidx.paging.PagingData
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.SortMode
import com.doro.music.data.repo.PlaylistRepo
import kotlinx.coroutines.flow.Flow

class GetPlaylistsUseCase(private val repo: PlaylistRepo) {

    operator fun invoke(): Flow<PagingData<Playlist>> = repo.getPlaylists(SortMode.DATE_ADDED)
}
