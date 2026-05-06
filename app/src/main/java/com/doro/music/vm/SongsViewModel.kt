@file:OptIn(ExperimentalCoroutinesApi::class)

package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.doro.music.base.BaseViewModel
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.UiEvent
import com.doro.music.data.repo.SongRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.domain.PlaySongsUseCase
import com.doro.music.data.repo.PlaybackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongsViewModel(
    private val repo: SongRepo,
    private val playbackRepository: PlaybackRepository,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val playSongsUseCase: PlaySongsUseCase
) : BaseViewModel() {

    val playlist = getPlaylistsUseCase().cachedIn(viewModelScope)

    val selectedSong: StateFlow<Long?>
        field = MutableStateFlow<Long?>(null)

    val songs = sortMode.flatMapLatest(repo::getSongs).cachedIn(viewModelScope)

    val songCount = repo.getSongCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0
    )

    fun selectSong(id: Long?) {
        selectedSong.tryEmit(id)
    }

    fun addSongToPlaylist(playlists: Set<Playlist>) {
        viewModelScope.launch {
            val songId = selectedSong.value ?: return@launch
            val result = safeCall { addSongToPlaylistUseCase(songId = songId, playlists = playlists.toList()) }
                .getOrDefault(AddSongResult.Failed)
            emitEvent(UiEvent.SongAddedToPlaylist(result))
        }
    }

    fun addToNext(song: Song) {
        viewModelScope.launch { playbackRepository.addToQueue(listOf(song)) }
    }

    fun play(song: Song? = null, randomize: Boolean = false) {
        viewModelScope.launch {
            val allSongs = repo.getAllSongs(sortMode.value)
            playSongsUseCase(allSongs, song, randomize)
        }
    }

}
