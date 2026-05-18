@file:OptIn(ExperimentalCoroutinesApi::class)

package com.doro.music.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.doro.music.base.BaseViewModel
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.SortMode
import com.doro.music.data.model.UiEvent
import com.doro.music.data.repo.PlaylistRepo
import com.doro.music.data.repo.SongListRepo
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayContext
import com.doro.music.player.PlayActionDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistRepo: PlaylistRepo,
    private val actionDispatcher: PlayActionDispatcher
) : BaseViewModel() {

    override fun getDefaultSortMode(): SortMode = SortMode.DATE_ADDED

    val dialogState: StateFlow<DialogState?>
        field  =  MutableStateFlow<DialogState?>(null)

    val playlists: Flow<PagingData<Playlist>> = sortMode
        .flatMapLatest { sort -> playlistRepo.getPlaylists(sort) }
        .cachedIn(viewModelScope)
    val playlistCount: StateFlow<Int> = playlistRepo.getPlaylistCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val success = safeCall { playlistRepo.createPlaylist(name) }.getOrDefault(false)
            emitEvent(UiEvent.PlaylistCreated(success))
            dismissDialog()
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            val success = safeCall { playlistRepo.deletePlaylist(playlistId) }.getOrDefault(false)
            emitEvent(UiEvent.PlaylistDeleted(success))
            dismissDialog()
        }
    }

    fun showCreatePlaylist() {
        dialogState.tryEmit(DialogState.Create)
    }

    fun showDeletePlaylist(playlistId: Long) {
        dialogState.tryEmit(DialogState.Delete(playlistId))
    }

    fun dismissDialog() {
        dialogState.tryEmit(null)
    }

    suspend fun checkDuplicateName(name: String) = playlistRepo.isPlaylistNameExists(name)

    fun addToNext(id: Long) {
            actionDispatcher.dispatch(PlayAction.InsertGroup(PlayContext.Playlist(id, SortMode.DATE_ADDED)))
    }

    sealed interface DialogState {
        data object Create : DialogState
        data class Delete(val playlistId: Long) : DialogState
    }
}