@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.main

import android.annotation.SuppressLint
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.DisplayMode
import com.doro.music.data.model.Folder
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.ui.component.DisplayItem
import com.doro.music.ui.component.DisplayList
import com.doro.music.ui.component.PlaylistSelectDialog
import com.doro.music.vm.FoldersViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object FolderList : NavKey

@Serializable
data class FolderSongs(val folderPath: String) : NavKey

@Composable
fun FoldersPage(
    vm: FoldersViewModel = koinViewModel(),
    onDetailClick: (song: Song) -> Unit,
) {
    val folders by vm.folders.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(FolderList)

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(rememberViewModelStoreNavEntryDecorator()),
        transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
        popTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
        predictivePopTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
        entryProvider = entryProvider {
            entry<FolderList> {
                FolderListView(
                    folders = folders,
                    onFolderClick = { path -> backStack.add(FolderSongs(path)) }
                )
            }
            entry<FolderSongs> { key ->
                FolderSongsRoute(
                    vm = vm,
                    folderPath = key.folderPath,
                    onDetailClick = onDetailClick,
                    onBack = { backStack.remove(key) }
                )
            }
        }
    )
}

@Composable
private fun FolderListView(
    folders: List<Folder>,
    onFolderClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp)
    ) {
        items(
            count = folders.size,
            key = { folders[it].path }
        ) { index ->
            val folder = folders[index]
            DisplayItem(
                mode = DisplayMode.LIST,
                title = folder.name,
                subtitle = stringResource(R.string.song_count, folder.songCount),
                albumArt = null,
                placeholderIcon = Icons.Filled.Folder,
                onClick = { onFolderClick(folder.path) }
            )
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun FolderSongsRoute(
    vm: FoldersViewModel,
    folderPath: String,
    onDetailClick: (Song) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val songs = vm.songs.collectAsLazyPagingItems()
    val playlist = vm.playlist.collectAsLazyPagingItems()
    val selectedSongId by vm.selectedSongId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(folderPath) {
        vm.selectFolder(folderPath)
    }

    LaunchedEffect(Unit) {
        vm.uiEvent.collectLatest { event ->
            snackbarHostState.showSnackbar(context.getString(event.messageResId))
        }
    }

    FolderSongsView(
        songs = songs,
        playlist = playlist,
        selectedSongId = selectedSongId,
        snackbarHostState = snackbarHostState,
        onAddToNext = vm::addToNext,
        onAddSongToPlaylist = vm::addSongToPlaylist,
        onSelectSong = vm::selectSong,
        onPlayFolderSongs = { song -> vm.playFolderSongs(folderPath, song) },
        onDetailClick = onDetailClick,
        onBack = onBack
    )
}

@Composable
private fun FolderSongsView(
    songs: LazyPagingItems<Song>,
    playlist: LazyPagingItems<Playlist>,
    selectedSongId: Long?,
    snackbarHostState: SnackbarHostState,
    onAddToNext: (Song) -> Unit,
    onAddSongToPlaylist: (Set<Playlist>) -> Unit,
    onSelectSong: (Long?) -> Unit,
    onPlayFolderSongs: (Song) -> Unit,
    onDetailClick: (Song) -> Unit,
    onBack: () -> Unit
) {
    PlaylistSelectDialog(
        show = selectedSongId != null,
        items = playlist,
        onConfirm = onAddSongToPlaylist,
        onDismiss = { onSelectSong(null) }
    )

    DisplayList(
        mode = DisplayMode.LIST,
        items = songs,
        snackbarHostState = snackbarHostState,
        topBar = {
            DisplayItem(
                mode = DisplayMode.LIST,
                title = "..",
                subtitle = "",
                albumArt = null,
                placeholderIcon = Icons.Filled.Folder,
                onClick = onBack
            )
        },
    ) { song ->
        DisplayItem(
            mode = DisplayMode.LIST,
            title = song.title,
            subtitle = song.artist ?: stringResource(R.string.unknown_artist),
            albumArt = song.albumArt,
            menu = {
                Option(onClick = { onAddToNext(song) }) { Text(text = stringResource(R.string.add_to_next)) }
                Option(onClick = { onSelectSong(song.id) }) { Text(text = stringResource(R.string.add_to_playlist)) }
                Option(onClick = { onDetailClick(song) }) { Text(text = stringResource(R.string.details)) }
            },
            onClick = { onPlayFolderSongs(song) }
        )
    }
}