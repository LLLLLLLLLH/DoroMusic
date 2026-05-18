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
import com.doro.music.player.model.PlayAction
import com.doro.music.player.PlayActionDispatcher
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
    private val actionDispatcher: PlayActionDispatcher,
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
        actionDispatcher.dispatch(PlayAction.Play(targetSong.id, context))
    }

    fun playAll() {
        viewModelScope.launch {
            val kw = keyword.value
            val sort = sortMode.value
            val allSongs = repo.getAllSongsByKeyWords(kw, sort).first()
            val firstSong = allSongs.firstOrNull() ?: return@launch
            val context = PlayContext.Search(kw, sort)
            actionDispatcher.dispatch(PlayAction.Play(firstSong.id, context))
        }
    }

    fun shufflePlay() {
        viewModelScope.launch {
            val kw = keyword.value
            val sort = sortMode.value
            val allSongs = repo.getAllSongsByKeyWords(kw, sort).first()
            val randomSong = allSongs.randomOrNull() ?: return@launch
            val context = PlayContext.Search(kw, sort)
            actionDispatcher.dispatch(PlayAction.Play(randomSong.id, context))
        }
    }

    fun addToNext(song: Song) {
        actionDispatcher.dispatch(PlayAction.InsertSingle(song.id))
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