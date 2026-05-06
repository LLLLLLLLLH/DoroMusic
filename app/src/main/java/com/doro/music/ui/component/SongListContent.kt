package com.doro.music.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.DisplayMode
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.model.SortMode

@Composable
fun SongListContent(
    modifier: Modifier = Modifier,
    songs: LazyPagingItems<Song>,
    playlists: LazyPagingItems<Playlist>,
    songCount: Int,
    selectedSong: Long?,
    displayMode: DisplayMode,
    sortMode: SortMode,
    snackbarHostState: SnackbarHostState,
    onSelectSong: (Long?) -> Unit,
    onAddSongToPlaylist: (Set<Playlist>) -> Unit,
    onAddToNext: (Song) -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    onSortChange: (SortMode) -> Unit,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onDetailClick: (Song) -> Unit,
    extraMenuItem: @Composable OptionMenuScope.(Song) -> Unit = {},
) {
    PlaylistSelectDialog(
        items = playlists,
        show = selectedSong != null,
        onDismiss = { onSelectSong(null) },
        onConfirm = onAddSongToPlaylist
    )

    DisplayList(
        modifier = modifier.fillMaxSize(),
        mode = displayMode,
        items = songs,
        key = { it.id },
        snackbarHostState = snackbarHostState,
        topBar = {
            ListHeader(
                modifier = Modifier.padding(10.dp),
                title = stringResource(R.string.song_count, songCount)
            ) {
                IconAction(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.play_all),
                    onClick = onPlayAll
                )
                IconAction(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = stringResource(R.string.shuffle_play),
                    onClick = onShufflePlay
                )
                SortMenu(
                    sortOptions = SortMode.entries,
                    sortBy = sortMode,
                    displayMode = displayMode,
                    onSortChange = onSortChange,
                    onDisplayModeChange = onDisplayModeChange,
                )
            }
        }
    ) { song ->
        DisplayItem(
            mode = displayMode,
            title = song.title,
            subtitle = song.artist ?: stringResource(R.string.unknown_artist),
            albumArt = song.albumArt,
            menu = {
                Option(onClick = { onAddToNext(song) }) { Text(stringResource(R.string.add_to_next)) }
                extraMenuItem(song)
                Option(onClick = { onDetailClick(song) }) { Text(stringResource(R.string.details)) }
            },
            onClick = { onSongClick(song) }
        )
    }
}
