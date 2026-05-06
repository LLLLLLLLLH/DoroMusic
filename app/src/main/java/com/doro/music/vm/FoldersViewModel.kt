package com.doro.music.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.doro.music.data.model.Folder
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.UiEvent
import com.doro.music.data.model.activeFolders
import com.doro.music.data.repo.FolderRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.domain.PlaySongsUseCase
import com.doro.music.data.repo.PlaybackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FoldersViewModel(
    private val repo: FolderRepo,
    private val playbackRepository: PlaybackRepository,
    private val playSongsUseCase: PlaySongsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase
) : ViewModel() {

    val folders: StateFlow<List<Folder>> = repo.folders
        .map { it.activeFolders() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val selectedFolder = MutableStateFlow<String?>(null)

    val songs = selectedFolder
        .filterNotNull()
        .flatMapLatest(repo::getSongsByFolder)
        .cachedIn(viewModelScope)

    val playlist = getPlaylistsUseCase().cachedIn(viewModelScope)

    val selectedSong: StateFlow<Long?>
        field = MutableStateFlow<Long?>(null)

    val uiEvent: SharedFlow<UiEvent>
        field = MutableSharedFlow<UiEvent>()

    fun selectFolder(path: String?) {
        selectedFolder.tryEmit(path)
    }

    fun selectSong(id: Long?) {
        selectedSong.tryEmit(id)
    }

    fun addToNext(song: Song) {
        viewModelScope.launch { playbackRepository.addToQueue(listOf(song)) }
    }

    fun addSongToPlaylist(playlists: Set<Playlist>) {
        val songId = selectedSong.value ?: return
        viewModelScope.launch {
            val result = addSongToPlaylistUseCase(songId = songId, playlists = playlists.toList())
            uiEvent.emit(UiEvent.SongAddedToPlaylist(result))
        }
    }

    fun playFolderSongs(folderPath: String, song: Song) {
        viewModelScope.launch {
            val songs = repo.getAllSongsByFolder(folderPath)
            playSongsUseCase(songs, song)
        }
    }
}
