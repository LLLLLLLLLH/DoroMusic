@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.Song
import com.doro.music.ext.safePop
import com.doro.music.ui.component.SongListContent
import com.doro.music.ui.screen.other.SongDetail
import com.doro.music.vm.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object Search : NavKey

fun EntryProviderScope<NavKey>.searchRoute(backStack: NavBackStack<NavKey>) {
    entry<Search> {
        SearchScreen(
            onBack = backStack::safePop,
            onDetailClick = { backStack.add(SongDetail(it)) }
        )
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun SearchScreen(
    vm: SearchViewModel = koinViewModel(),
    onDetailClick: (Song) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val songs = vm.searchResults.collectAsLazyPagingItems()
    val keyword by vm.keyword.collectAsStateWithLifecycle()
    val displayMode by vm.displayMode.collectAsStateWithLifecycle()
    val sortMode by vm.sortMode.collectAsStateWithLifecycle()
    val songCount by vm.songCount.collectAsStateWithLifecycle()
    val playlists = vm.playlists.collectAsLazyPagingItems()
    val selectedSong by vm.selectedSong.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.uiEvent.collectLatest { event ->
            snackbarHostState.showSnackbar(message = context.getString(event.messageResId))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    value = keyword,
                    onValueChange = vm::searchSongs,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )
            }
        }
    ) { paddingValues ->
        SongListContent(
            modifier = Modifier.padding(paddingValues),
            songs = songs,
            playlists = playlists,
            songCount = songCount,
            selectedSong = selectedSong,
            displayMode = displayMode,
            sortMode = sortMode,
            snackbarHostState = snackbarHostState,
            onSelectSong = vm::selectSong,
            onAddSongToPlaylist = vm::addSongToPlaylist,
            onAddToNext = vm::addToNext,
            onSongClick = { vm.play(song = it) },
            onPlayAll = { vm.play() },
            onShufflePlay = { vm.play(randomize = true) },
            onSortChange = vm::setSortBy,
            onDisplayModeChange = vm::setDisplayMode,
            onDetailClick = onDetailClick,
            extraMenuItem = { song ->
                Option(onClick = { vm.selectSong(song.id) }) { Text(stringResource(R.string.add_to_playlist)) }
            }
        )
    }
}
