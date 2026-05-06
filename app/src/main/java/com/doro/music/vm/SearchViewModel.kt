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
import com.doro.music.domain.PlaySongsUseCase
import com.doro.music.data.repo.PlaybackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repo: SearchRepo,
    private val playbackRepository: PlaybackRepository,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val playSongsUseCase: PlaySongsUseCase
) : BaseViewModel() {

    val playlists = getPlaylistsUseCase().cachedIn(viewModelScope)

    val keyword: StateFlow<String>
        field = MutableStateFlow<String>("")

    val selectedSong: StateFlow<Long?>
        field = MutableStateFlow<Long?>(null)

    val searchResults = combine(keyword, sortMode) { kw, sort -> kw to sort }
        .flatMapLatest { (kw, sort) -> repo.getSongsByKeyWords(kw, sort) }
        .cachedIn(viewModelScope)

    val songCount = keyword.flatMapLatest { kw ->
        repo.getSongCountByKeyWords(kw)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0
    )

    fun searchSongs(keyword: String) {
        this.keyword.value = keyword
    }

    fun play(song: Song? = null, randomize: Boolean = false) {
        val kw = keyword.value
        val sort = sortMode.value
        viewModelScope.launch {
            val songs = repo.getAllSongsByKeyWords(kw, sort).first()
            playSongsUseCase(songs, song, randomize)
        }
    }

    fun addToNext(song: Song) {
        viewModelScope.launch { playbackRepository.addToQueue(listOf(song)) }
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
