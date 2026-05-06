package com.doro.music.ui.screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.Artist
import com.doro.music.data.model.DisplayMode
import com.doro.music.data.model.SortMode
import com.doro.music.ui.component.DisplayItem
import com.doro.music.ui.component.DisplayList
import com.doro.music.ui.component.ListHeader
import com.doro.music.ui.component.SortMenu
import com.doro.music.vm.ArtistsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArtistsPage(
    vm: ArtistsViewModel = koinViewModel(),
    onArtistClick: (artist: Artist) -> Unit = {},
) {
    val artists = vm.artists.collectAsLazyPagingItems()
    val artistCount by vm.artistCount.collectAsStateWithLifecycle()
    val displayMode by vm.displayMode.collectAsStateWithLifecycle()
    val sortMode by vm.sortMode.collectAsStateWithLifecycle()

    DisplayList(
        modifier = Modifier.fillMaxSize(),
        mode = displayMode,
        items = artists,
        topBar = {
            ListHeader(
                modifier = Modifier.padding(10.dp),
                title = stringResource(R.string.artist_count, artistCount)
            ) {
                SortMenu(
                    sortOptions = listOf(SortMode.TITLE, SortMode.ARTIST),
                    sortBy = sortMode,
                    displayMode = displayMode,
                    onSortChange = vm::setSortBy,
                    onDisplayModeChange = vm::setDisplayMode,
                )
            }
        }
    ) { item ->
        DisplayItem(
            mode = displayMode,
            title = item.name,
            subtitle = stringResource(R.string.song_count, item.songCount),
            placeholderIcon = Icons.Rounded.Person,
            menu = {
                Option(onClick = { vm.addArtistToNext(item.name) }) {
                    Text(stringResource(R.string.add_to_next))
                }
            },
            onClick = { onArtistClick(item) },
        )
    }
}
