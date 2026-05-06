package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.doro.music.base.BaseViewModel
import com.doro.music.data.model.Artist
import com.doro.music.data.repo.ArtistRepo
import com.doro.music.data.repo.SongListRepo
import com.doro.music.data.repo.PlaybackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArtistsViewModel(
    private val repo: ArtistRepo,
    private val songListRepo: SongListRepo,
    private val playbackRepository: PlaybackRepository
) : BaseViewModel() {

    val artists: Flow<PagingData<Artist>> = repo.getArtists().cachedIn(viewModelScope)

    val artistCount = repo.getArtistCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    fun addArtistToNext(name: String) {
        viewModelScope.launch {
            val songs = songListRepo.getAllSongsByArtist(name)
            if (songs.isNotEmpty()) playbackRepository.addToQueue(songs)
        }
    }
}
