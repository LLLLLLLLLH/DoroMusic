@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.DisplayMode
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.SortMode
import com.doro.music.ui.component.DisplayItem
import com.doro.music.ui.component.DisplayList
import com.doro.music.ui.component.IconAction
import com.doro.music.ui.component.ListHeader
import com.doro.music.ui.component.SortMenu
import com.doro.music.vm.PlaylistsViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun PlaylistsPage(
    vm: PlaylistsViewModel = koinViewModel(),
    onPlaylistClick: (Playlist) -> Unit = {},
) {
    val context = LocalContext.current
    val playlists = vm.playlists.collectAsLazyPagingItems()
    val playlistCount by vm.playlistCount.collectAsStateWithLifecycle()
    val dialogState by vm.dialogState.collectAsStateWithLifecycle()
    val sortMode by vm.sortMode.collectAsStateWithLifecycle()
    val displayMode by vm.displayMode.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.uiEvent.collectLatest { event ->
            snackbarHostState.showSnackbar(message = context.getString(event.messageResId))
        }
    }

    PlaylistsPageContent(
        playlists = playlists,
        playlistCount = playlistCount,
        sortMode = sortMode,
        displayMode = displayMode,
        snackbarHostState = snackbarHostState,
        onAddPlaylist = vm::showCreatePlaylist,
        onDeletePlaylist = vm::showDeletePlaylist,
        onPlaylistClick = onPlaylistClick,
        onAddToNext = vm::addToNext,
        onSortChange = vm::setSortBy,
        onDisplayModeChange = vm::setDisplayMode
    )

    HandleDialog(
        state = dialogState,
        onCreateConfirm = vm::createPlaylist,
        onDeleteConfirm = vm::deletePlaylist,
        checkDuplicateName = vm::checkDuplicateName,
        onDismiss = vm::dismissDialog
    )
}

@Composable
private fun PlaylistsPageContent(
    playlists: LazyPagingItems<Playlist>,
    playlistCount: Int,
    sortMode: SortMode,
    displayMode: DisplayMode,
    snackbarHostState: SnackbarHostState,
    onAddPlaylist: () -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAddToNext: (Long) -> Unit,
    onSortChange: (SortMode) -> Unit,
    onDisplayModeChange: (DisplayMode) -> Unit
) {
    DisplayList(
        modifier = Modifier.fillMaxSize(),
        mode = displayMode,
        items = playlists,
        snackbarHostState = snackbarHostState,
        topBar = {
            ListHeader(
                modifier = Modifier.padding(10.dp),
                title = stringResource(R.string.playlist_count, playlistCount)
            ) {
                IconAction(
                    imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                    contentDescription = stringResource(R.string.create_playlist),
                    onClick = onAddPlaylist
                )
                SortMenu(
                    sortOptions = listOf(SortMode.TITLE, SortMode.DATE_ADDED),
                    sortBy = sortMode,
                    displayMode = displayMode,
                    onSortChange = onSortChange,
                    onDisplayModeChange = onDisplayModeChange,
                )
            }
        }
    ) { playlist ->
        PlaylistItem(
            displayMode = displayMode,
            playlist = playlist,
            onPlaylistClick = onPlaylistClick,
            onDeletePlaylist = onDeletePlaylist,
            onAddToNext = onAddToNext
        )
    }
}


@Composable
private fun PlaylistItem(
    displayMode: DisplayMode,
    playlist: Playlist,
    onPlaylistClick: (Playlist) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onAddToNext: (Long) -> Unit
) {

    DisplayItem(
        mode = displayMode,
        title = playlist.name,
        subtitle = stringResource(R.string.song_count, playlist.songCount),
        albumArt = null,
        menu = {
            Option(onClick = { onAddToNext(playlist.id) }) {
                Text(stringResource(R.string.add_to_next))
            }
            Option(onClick = { onDeletePlaylist(playlist.id) }) {
                Text(stringResource(R.string.delete))
            }
        },
        onClick = { onPlaylistClick(playlist) }
    )
}

@Composable
private fun HandleDialog(
    state: PlaylistsViewModel.DialogState?,
    onCreateConfirm: (name: String) -> Unit,
    onDeleteConfirm: (id: Long) -> Unit,
    checkDuplicateName: suspend (String) -> Boolean,
    onDismiss: () -> Unit
) {
    when (state) {
        PlaylistsViewModel.DialogState.Create -> CreatePlaylistDialog(
            onConfirm = onCreateConfirm,
            onDismiss = onDismiss,
            checkDuplicateName = checkDuplicateName
        )

        is PlaylistsViewModel.DialogState.Delete -> DeletePlaylistDialog(
            onConfirm = { onDeleteConfirm(state.playlistId) },
            onDismiss = onDismiss
        )

        else -> Unit
    }
}

@Composable
private fun CreatePlaylistDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    checkDuplicateName: suspend (String) -> Boolean,
) {
    var name by remember { mutableStateOf("") }

    val isDuplicateName by produceState(initialValue = false, key1 = name) {
        value = if (name.isNotBlank()) checkDuplicateName(name.trim()) else false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.create_playlist)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.playlist_name_hint)) },
                singleLine = true,
                isError = isDuplicateName,
                supportingText = { if (isDuplicateName) Text(text = stringResource(R.string.playlist_already_exists)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank() && !isDuplicateName
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DeletePlaylistDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_playlist)) },
        text = { Text(stringResource(R.string.delete_playlist_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}