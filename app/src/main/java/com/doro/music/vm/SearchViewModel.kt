@file:OptIn(ExperimentalCoroutinesApi::class)

package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.doro.music.base.BaseViewModel
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.UiEvent
import com.doro.music.data.repo.SearchRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.domain.PlaybackUseCase
import com.doro.music.player.model.PlayContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repo: SearchRepo,
    private val playbackUseCase: PlaybackUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase
) : BaseViewModel() {

    val playlists = getPlaylistsUseCase().cachedIn(viewModelScope)
    val keyword: StateFlow<String>
        field = MutableStateFlow("")
    val selectedSong: StateFlow<Long?>
        field = MutableStateFlow<Long?>(null)

    val searchResults = combine(keyword, sortMode) { kw, sort -> kw to sort }
        .flatMapLatest { (kw, sort) -> repo.getSongsByKeyWords(kw, sort) }
        .cachedIn(viewModelScope)

    val songCount = keyword.flatMapLatest { kw -> repo.getSongCountByKeyWords(kw) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    fun searchSongs(keyword: String) {
        this.keyword.tryEmit(keyword)
    }

    fun play(song: Song? = null) {
        val targetSong = song ?: return
        val kw = keyword.value
        val sort = sortMode.value
        val context = PlayContext.Search(kw, sort)
        playbackUseCase.play(targetSong, context)
    }

    fun playAll() {
        viewModelScope.launch {
            val kw = keyword.value
            val sort = sortMode.value
            val allSongs = repo.getAllSongsByKeyWords(kw, sort).first()
            val context = PlayContext.Search(kw, sort)
            playbackUseCase.playFirst(allSongs, context)
        }
    }

    fun shufflePlay() {
        viewModelScope.launch {
            val kw = keyword.value
            val sort = sortMode.value
            val allSongs = repo.getAllSongsByKeyWords(kw, sort).first()
            val context = PlayContext.Search(kw, sort)
            playbackUseCase.shufflePlay(allSongs, context)
        }
    }

    fun addToNext(song: Song) {
        playbackUseCase.addToNext(song)
    }

    fun addSongToPlaylist(playlists: Set<Playlist>) {
        val songId = selectedSong.value ?: return
        viewModelScope.launch {
            val result = safeCall { addSongToPlaylistUseCase(songId = songId, playlists = playlists.toList()) }
                .getOrDefault(AddSongResult.Failed)
            emitEvent(UiEvent.SongAddedToPlaylist(result))
        }
    }

    fun selectSong(id: Long?) {
        selectedSong.tryEmit(id)
    }
}
