package com.doro.music.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.doro.music.data.model.DisplayMode

@Composable
fun <T : Any> DisplayList(
    modifier: Modifier = Modifier,
    mode: DisplayMode,
    items: LazyPagingItems<T>,
    key: ((T) -> Any)? = null,
    snackbarHostState: SnackbarHostState? = null,
    topBar: @Composable () -> Unit = {},
    content: @Composable (T) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        snackbarHost = { snackbarHostState?.let { SnackbarHost(hostState = it) } }
    ) {
        when (mode) {
            DisplayMode.GRID -> GridList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                items = items,
                key = key,
                content = content
            )

            DisplayMode.LIST, DisplayMode.COMPACT -> LinearList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                items = items,
                key = key,
                content = content
            )
        }
    }
}

@Composable
private fun <T : Any> LinearList(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<T>,
    key: ((T) -> Any)? = null,
    content: @Composable (item: T) -> Unit
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(
            count = items.itemCount,
            key = { index -> items[index]?.let { key?.invoke(it) } ?: index }
        ) { index ->
            items[index]?.let { content(it) }
        }
    }
}

@Composable
private fun <T : Any> GridList(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<T>,
    key: ((T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    content: @Composable (item: T) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            count = items.itemCount,
            key = { index -> items[index]?.let { key?.invoke(it) } ?: index }
        ) { index ->
            items[index]?.let { content(it) }
        }
    }
}
