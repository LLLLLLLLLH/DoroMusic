package com.doro.music.ui.screen.main

import android.annotation.SuppressLint
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.Song
import com.doro.music.ui.component.SongListContent
import com.doro.music.vm.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun SongsPage(
    vm: SongsViewModel = koinViewModel(),
    onDetailClick: (Song) -> Unit = {},
) {
    val context = LocalContext.current
    val songs = vm.songs.collectAsLazyPagingItems()
    val playlist = vm.playlist.collectAsLazyPagingItems()
    val songCount by vm.songCount.collectAsStateWithLifecycle()
    val sortMode by vm.sortMode.collectAsStateWithLifecycle()
    val displayMode by vm.displayMode.collectAsStateWithLifecycle()
    val selectedSongId by vm.selectedSongId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.uiEvent.collectLatest { event ->
            snackbarHostState.showSnackbar(message = context.getString(event.messageResId))
        }
    }

    SongListContent(
        songs = songs,
        playlists = playlist,
        songCount = songCount,
        selectedSongId = selectedSongId,
        displayMode = displayMode,
        sortMode = sortMode,
        snackbarHostState = snackbarHostState,
        onSelectSong = vm::selectSong,
        onAddSongToPlaylist = vm::addSongToPlaylist,
        onAddToNext = vm::addToNext,
        onSongClick = vm::play,
        onPlayAll = vm::playAll,
        onShufflePlay = vm::shufflePlay,
        onSortChange = vm::setSortBy,
        onDisplayModeChange = vm::setDisplayMode,
        onDetailClick = onDetailClick,
        extraMenuItem = { song ->
            Option(onClick = { vm.selectSong(song.id) }) { Text(stringResource(R.string.add_to_playlist)) }
        }
    )
}
