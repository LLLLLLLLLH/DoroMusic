package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.doro.music.base.BaseViewModel
import com.doro.music.data.model.Artist
import com.doro.music.data.repo.ArtistRepo
import com.doro.music.domain.PlaybackUseCase
import com.doro.music.player.model.PlayContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ArtistsViewModel(
    private val repo: ArtistRepo,
    private val playbackUseCase: PlaybackUseCase
) : BaseViewModel() {

    val artists: Flow<PagingData<Artist>> = repo.getArtists().cachedIn(viewModelScope)
    val artistCount = repo.getArtistCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    fun addArtistToNext(name: String) {
        playbackUseCase.addGroupToNext(PlayContext.Artist(name, sortMode.value))
    }
}
