@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.other

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.Artist
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.ui.component.SongListContent
import com.doro.music.vm.SongListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
sealed class SongListSource(val title: String) {

    @Serializable
    data class FromArtist(val artist: Artist) : SongListSource(artist.name)

    @Serializable
    data class FromPlaylist(val playlist: Playlist) : SongListSource(playlist.name)
}

@Serializable
data class SongList(val source: SongListSource) : NavKey

fun EntryProviderScope<NavKey>.songListScreen(
    onBack: () -> Unit,
    onSongDetailClick: (song: Song) -> Unit,
) {
    entry<SongList> {
        SongListScreen(
            source = it.source,
            onBack = onBack,
            onSongDetailClick = onSongDetailClick
        )
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun SongListScreen(
    vm: SongListViewModel = koinViewModel(),
    source: SongListSource,
    onSongDetailClick: (song: Song) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sortBy by vm.sortMode.collectAsStateWithLifecycle()
    val displayMode by vm.displayMode.collectAsStateWithLifecycle()
    val songs = vm.songs.collectAsLazyPagingItems()
    val playlist = vm.playlist.collectAsLazyPagingItems()
    val songCount by vm.songCount.collectAsStateWithLifecycle()
    val selectedSongId by vm.selectedSongId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(source) {
        vm.setSource(source)
    }

    LaunchedEffect(Unit) {
        vm.uiEvent.collectLatest { event ->
            snackbarHostState.showSnackbar(message = context.getString(event.messageResId))
        }
    }

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state = topAppBarState)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { Text(text = source.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = Icons.Rounded.ArrowBackIosNew.name
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        SongListContent(
            modifier = Modifier.padding(paddingValues),
            songs = songs,
            playlists = playlist,
            songCount = songCount,
            selectedSongId = selectedSongId,
            displayMode = displayMode,
            sortMode = sortBy,
            snackbarHostState = snackbarHostState,
            onSelectSong = vm::selectSong,
            onAddSongToPlaylist = vm::addSongToPlaylist,
            onAddToNext = vm::addToNext,
            onSongClick = { vm.play(song = it) },
            onPlayAll = { vm.playAll() },
            onShufflePlay = { vm.shufflePlay() },
            onSortChange = vm::setSortBy,
            onDisplayModeChange = vm::setDisplayMode,
            onDetailClick = onSongDetailClick,
            extraMenuItem = { song ->
                when (source) {
                    is SongListSource.FromArtist -> Option(onClick = { vm.selectSong(song.id) }) { Text(stringResource(R.string.add_to_playlist)) }
                    is SongListSource.FromPlaylist -> Option(onClick = { vm.removeSongFromPlaylist(source.playlist.id, song.id) }) { Text(stringResource(R.string.remove_the_playlist)) }
                }
            }
        )
    }
}
