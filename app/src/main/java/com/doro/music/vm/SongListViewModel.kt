@file:OptIn(ExperimentalCoroutinesApi::class)

package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.doro.music.base.BaseViewModel
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.UiEvent
import com.doro.music.data.repo.SongListRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayContext
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.ui.screen.other.SongListSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongListViewModel(
    private val repo: SongListRepo,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val dispatcher: PlayActionDispatcher
) : BaseViewModel() {

    val playlist = getPlaylistsUseCase().cachedIn(viewModelScope)
    val selectedSong: StateFlow<Long?>
        field = MutableStateFlow<Long?>(null)
    private val source = MutableStateFlow<SongListSource?>(null)

    val songs = source.filterNotNull().flatMapLatest {
        when (it) {
            is SongListSource.FromArtist -> repo.getSongListByArtist(it.artist.name)
            is SongListSource.FromPlaylist -> repo.getSongListByPlaylist(it.playlist.id)
        }
    }.cachedIn(viewModelScope)

    val songCount = source.filterNotNull().flatMapLatest {
        when (it) {
            is SongListSource.FromArtist -> repo.getSongCountByArtist(it.artist.name)
            is SongListSource.FromPlaylist -> repo.getSongCountByPlaylist(it.playlist.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    fun setSource(source: SongListSource) {
        this.source.tryEmit(source)
    }

    fun play(song: Song? = null) {
        val currentSource = source.value ?: return
        val targetSongId = song?.id ?: return
        val context = when (currentSource) {
            is SongListSource.FromArtist -> PlayContext.Artist(currentSource.artist.name, sortMode.value)
            is SongListSource.FromPlaylist -> PlayContext.Playlist(currentSource.playlist.id, sortMode.value)
        }
        dispatcher.dispatch(PlayAction.Play(targetSongId, context))
    }

    fun playAll() {
        viewModelScope.launch {
            val currentSource = source.value ?: return@launch
            val allSongs = when (currentSource) {
                is SongListSource.FromArtist -> repo.getAllSongsByArtist(currentSource.artist.name)
                is SongListSource.FromPlaylist -> repo.getAllSongsByPlaylist(currentSource.playlist.id)
            }
            val firstSong = allSongs.firstOrNull() ?: return@launch
            val context = when (currentSource) {
                is SongListSource.FromArtist -> PlayContext.Artist(currentSource.artist.name, sortMode.value)
                is SongListSource.FromPlaylist -> PlayContext.Playlist(currentSource.playlist.id, sortMode.value)
            }
            dispatcher.dispatch(PlayAction.Play(firstSong.id, context))
        }
    }

    fun shufflePlay() {
        viewModelScope.launch {
            val currentSource = source.value ?: return@launch
            val allSongs = when (currentSource) {
                is SongListSource.FromArtist -> repo.getAllSongsByArtist(currentSource.artist.name)
                is SongListSource.FromPlaylist -> repo.getAllSongsByPlaylist(currentSource.playlist.id)
            }
            val randomSong = allSongs.randomOrNull() ?: return@launch
            val context = when (currentSource) {
                is SongListSource.FromArtist -> PlayContext.Artist(currentSource.artist.name, sortMode.value)
                is SongListSource.FromPlaylist -> PlayContext.Playlist(currentSource.playlist.id, sortMode.value)
            }
            dispatcher.dispatch(PlayAction.Play(randomSong.id, context))
        }
    }

    fun addToNext(song: Song) {
        viewModelScope.launch { dispatcher.dispatch(PlayAction.InsertSingle(song.id)) }
    }

    fun addSongToPlaylist(playlists: Set<Playlist>) {
        val songId = selectedSong.value ?: return
        viewModelScope.launch {
            val result = safeCall { addSongToPlaylistUseCase(songId = songId, playlists = playlists.toList()) }
                .getOrDefault(AddSongResult.Failed)
            emitEvent(UiEvent.SongAddedToPlaylist(result))
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            val success = safeCall { repo.removeSongFromPlaylist(playlistId, songId) }.getOrDefault(false)
            emitEvent(UiEvent.SongRemovedFromPlaylist(success))
        }
    }

    fun selectSong(id: Long?) {
        selectedSong.tryEmit(id)
    }
}