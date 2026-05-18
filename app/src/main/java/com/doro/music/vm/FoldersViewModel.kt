@file:OptIn(ExperimentalCoroutinesApi::class)

package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.doro.music.base.BaseViewModel
import com.doro.music.data.model.Folder
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.UiEvent
import com.doro.music.data.model.activeFolders
import com.doro.music.data.repo.FolderRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayContext
import com.doro.music.player.PlayActionDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoldersViewModel(
    private val repo: FolderRepo,
    private val actionDispatcher: PlayActionDispatcher,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase
) : BaseViewModel() {

    val folders: StateFlow<List<Folder>> = repo.folders
        .map { it.activeFolders() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val selectedFolder = MutableStateFlow<String?>(null)
    val songs = selectedFolder.filterNotNull().flatMapLatest(repo::getSongsByFolder).cachedIn(viewModelScope)
    val playlist = getPlaylistsUseCase().cachedIn(viewModelScope)
    val selectedSong: StateFlow<Long?>
     field = MutableStateFlow<Long?>(null)

    fun selectFolder(path: String?) { selectedFolder.tryEmit(path) }
    fun selectSong(id: Long?) { selectedSong.tryEmit(id) }

    fun addToNext(song: Song) {
        actionDispatcher.dispatch(PlayAction.InsertSingle(song.id))
    }

    fun addSongToPlaylist(playlists: Set<Playlist>) {
        val songId = selectedSong.value ?: return
        viewModelScope.launch {
            val result = addSongToPlaylistUseCase(songId = songId, playlists = playlists.toList())
            emitEvent(UiEvent.SongAddedToPlaylist(result))
        }
    }

    fun playFolderSongs(folderPath: String, song: Song) {
        val context = PlayContext.Folder(folderPath, com.doro.music.data.model.SortMode.TITLE)
        actionDispatcher.dispatch(PlayAction.Play(song.id, context))
    }
}