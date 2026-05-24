@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.main

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.doro.music.R
import com.doro.music.data.model.Artist
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.ext.noRippleClick
import com.doro.music.ui.component.IconAction
import com.doro.music.ui.component.rememberAudioPermissionState
import com.doro.music.vm.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object Main : NavKey

enum class MainTab(@StringRes val titleRes: Int) {
    Songs(R.string.songs),
    Artists(R.string.artists),
    Folders(R.string.folders),
    Playlists(R.string.playlists)
}


@Immutable
private object MainScreenDefaults {
    val TAB_INDICATOR_HORIZONTAL_PADDING = 10.dp
    val TAB_INDICATOR_VERTICAL_PADDING = 8.dp
    val TAB_INDICATOR_CORNER_SIZE = 4.dp
    const val TAB_INDICATOR_BACKGROUND_ALPHA = 0.2f
    const val TAB_COLOR_ANIMATION_DURATION_MS = 300
    val TAB_ROW_EDGE_PADDING = 10.dp
    val REFRESH_INDICATOR_SIZE = 24.dp
    val REFRESH_INDICATOR_STROKE_WIDTH = 2.5.dp
}

fun EntryProviderScope<NavKey>.mainScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSongDetailClick: (song: Song) -> Unit,
    onArtistClick: (artist: Artist) -> Unit,
    onPlaylistClick: (playlist: Playlist) -> Unit,
) {
    entry<Main> {
        MainScreenRoute(
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick,
            onSongDetailClick = onSongDetailClick,
            onArtistClick = onArtistClick,
            onPlaylistClick = onPlaylistClick,
        )
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun MainScreenRoute(
    vm: MainViewModel = koinViewModel(),
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSongDetailClick: (song: Song) -> Unit = {},
    onArtistClick: (artist: Artist) -> Unit = {},
    onPlaylistClick: (playlist: Playlist) -> Unit = {},
) {
    val context = LocalContext.current
    val scanState by vm.scanState.collectAsStateWithLifecycle()
    val snackBarState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val isLoading by remember { derivedStateOf { scanState is MainViewModel.ScanState.Scanning } }

    LaunchedEffect(vm.scanEvent, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.scanEvent.collectLatest { event ->
                val message = when (event) {
                    is MainViewModel.ScanState.Done -> context.getString(R.string.scan_completed, event.count)
                    MainViewModel.ScanState.Error -> context.getString(R.string.media_store_error)
                    else -> return@collectLatest
                }
                snackBarState.showSnackbar(message)
            }
        }
    }

    val permissionState = rememberAudioPermissionState(
        onGranted = vm::scan, onDenied = { isPermanentlyDenied ->
            val messageId = if (isPermanentlyDenied) R.string.permission_permanently_denied else R.string.permission_storage_required
            Toast.makeText(context, context.getString(messageId), Toast.LENGTH_LONG).show()
        })

    MainScreen(
        isLoading = isLoading,
        snackBarState = snackBarState,
        onScanClick = { permissionState.request() },
        onSearchClick = onSearchClick,
        onSettingsClick = onSettingsClick,
        onSongDetailClick = onSongDetailClick,
        onArtistClick = onArtistClick,
        onPlaylistClick = onPlaylistClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    isLoading: Boolean,
    snackBarState: SnackbarHostState,
    onScanClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSongDetailClick: (song: Song) -> Unit,
    onArtistClick: (artist: Artist) -> Unit,
    onPlaylistClick: (playlist: Playlist) -> Unit,
) {
    val tabs = MainTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state = topAppBarState)
    val scope = rememberCoroutineScope()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MainTopAppBar(
                scrollBehavior = scrollBehavior,
                isLoading = isLoading,
                onSearchClick = onSearchClick,
                onScanRequest = onScanClick,
                onSettingsClick = onSettingsClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            MainScreenTabRow(
                tabs = tabs,
                selectedTabIndex = pagerState.currentPage,
                onTabClick = { index -> scope.launch { pagerState.animateScrollToPage(index) } })

            HorizontalPager(
                state = pagerState, modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                MainTabContent(
                    tab = tabs[pageIndex],
                    onDetailClick = onSongDetailClick,
                    onArtistClick = onArtistClick,
                    onPlaylistClick = onPlaylistClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    isLoading: Boolean,
    onSearchClick: () -> Unit,
    onScanRequest: () -> Unit,
    onSettingsClick: () -> Unit
) {
    LargeTopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconAction(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search),
                onClick = onSearchClick
            )
            IconButton(
                onClick = onScanRequest,
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(MainScreenDefaults.REFRESH_INDICATOR_SIZE),
                        strokeWidth = MainScreenDefaults.REFRESH_INDICATOR_STROKE_WIDTH,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            IconAction(
                imageVector = Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.settings),
                onClick = onSettingsClick
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenTabRow(
    modifier: Modifier = Modifier,
    tabs: List<MainTab>,
    selectedTabIndex: Int,
    onTabClick: (Int) -> Unit
) {
    PrimaryScrollableTabRow(
        modifier = modifier,
        edgePadding = MainScreenDefaults.TAB_ROW_EDGE_PADDING,
        selectedTabIndex = selectedTabIndex,
        indicator = {
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(selectedTabIndex)
                    .padding(
                        horizontal = MainScreenDefaults.TAB_INDICATOR_HORIZONTAL_PADDING,
                        vertical = MainScreenDefaults.TAB_INDICATOR_VERTICAL_PADDING
                    )
                    .fillMaxSize()
                    .clip(RoundedCornerShape(MainScreenDefaults.TAB_INDICATOR_CORNER_SIZE))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = MainScreenDefaults.TAB_INDICATOR_BACKGROUND_ALPHA))
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, tab ->
            MainTabItem(
                selected = selectedTabIndex == index, onClick = {
                    if (selectedTabIndex != index) {
                        onTabClick(index)
                    }
                }, tab = tab
            )
        }
    }
}

@Composable
private fun MainTabItem(
    selected: Boolean, onClick: () -> Unit, tab: MainTab
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = MainScreenDefaults.TAB_COLOR_ANIMATION_DURATION_MS),
        label = "tabContentColor"
    )

    Box(
        modifier = Modifier
            .noRippleClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(tab.titleRes),
            color = contentColor,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun MainTabContent(
    tab: MainTab,
    onDetailClick: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    when (tab) {
        MainTab.Songs -> SongsPage(onDetailClick = onDetailClick)
        MainTab.Artists -> ArtistsPage(onArtistClick = onArtistClick)
        MainTab.Folders -> FoldersPage(onDetailClick = onDetailClick)
        MainTab.Playlists -> PlaylistsPage(onPlaylistClick = onPlaylistClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainTopAppBarPreview_Idle() {
    MaterialTheme {
        MainTopAppBar(
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            isLoading = false,
            onSearchClick = {},
            onScanRequest = {},
            onSettingsClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainTopAppBarPreview_Loading() {
    MaterialTheme {
        MainTopAppBar(
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            isLoading = true,
            onSearchClick = {},
            onScanRequest = {},
            onSettingsClick = {})
    }
}