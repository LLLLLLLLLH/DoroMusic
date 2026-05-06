@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.main

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.doro.music.R
import com.doro.music.data.model.Artist
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.ui.component.IconAction
import com.doro.music.ui.component.MainTab
import com.doro.music.ui.component.rememberAudioPermissionState
import com.doro.music.vm.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object Main : NavKey

private object MainScreenDefaults {

    @Stable
    val TabIndicatorHorizontalPadding = 10.dp

    @Stable
    val TabIndicatorVerticalPadding = 8.dp

    @Stable
    val TabIndicatorCornerSize = 4.dp

    @Stable
    val TabIndicatorBackgroundAlpha = 0.2f

    @Stable
    val TabColorAnimationDurationMs = 300

    @Stable
    val TabRowEdgePadding = 10.dp

    @Stable
    val RefreshIndicatorSize = 24.dp

    @Stable
    val RefreshIndicatorStrokeWidth = 2.5.dp
}

fun EntryProviderScope<NavKey>.mainScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSongDetailClick: (song: Song) -> Unit,
    onArtistClick: (artist: Artist) -> Unit,
    onPlaylistClick: (playlist: Playlist) -> Unit,
) {
    entry<Main> {
        MainScreen(
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
private fun MainScreen(
    vm: MainViewModel = koinViewModel(),
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSongDetailClick: (song: Song) -> Unit = {},
    onArtistClick: (artist: Artist) -> Unit = {},
    onPlaylistClick: (playlist: Playlist) -> Unit = {},
) {
    val context = LocalContext.current
    val tabs = MainTab.entries
    val scanState by vm.scanState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { tabs.size }
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state = topAppBarState)
    val snackBarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isLoading = scanState is MainViewModel.ScanState.Scanning

    val permissionState = rememberAudioPermissionState(
        onGranted = vm::scan,
        onDenied = { isPermanentlyDenied ->
            val messageId = if (isPermanentlyDenied) R.string.permission_permanently_denied else R.string.permission_storage_required
            Toast.makeText(context, context.getString(messageId), Toast.LENGTH_LONG).show()
        }
    )

    LaunchedEffect(Unit) {
        vm.scanEvent.collectLatest { event ->
            val message = when (event) {
                is MainViewModel.ScanState.Done -> context.getString(R.string.scan_completed, event.count)
                MainViewModel.ScanState.Error -> context.getString(R.string.media_store_error)
                else -> return@collectLatest
            }
            snackBarState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconAction(imageVector = Icons.Rounded.Search, contentDescription = stringResource(R.string.search), onClick = onSearchClick)
                    IconButton(
                        onClick = { permissionState.request() },
                        enabled = !isLoading,
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(MainScreenDefaults.RefreshIndicatorSize),
                                strokeWidth = MainScreenDefaults.RefreshIndicatorStrokeWidth,
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
        },
        snackbarHost = { SnackbarHost(hostState = snackBarState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            PrimaryScrollableTabRow(
                edgePadding = MainScreenDefaults.TabRowEdgePadding,
                selectedTabIndex = pagerState.currentPage,
                indicator = {
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(pagerState.currentPage)
                            .padding(
                                horizontal = MainScreenDefaults.TabIndicatorHorizontalPadding,
                                vertical = MainScreenDefaults.TabIndicatorVerticalPadding
                            )
                            .fillMaxSize()
                            .clip(RoundedCornerShape(MainScreenDefaults.TabIndicatorCornerSize))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = MainScreenDefaults.TabIndicatorBackgroundAlpha))
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    MainTabItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            if (pagerState.currentPage != index) {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                        },
                        tab = tab
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
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

@Composable
private fun MainTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    tab: MainTab
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = MainScreenDefaults.TabColorAnimationDurationMs),
        label = "tabContentColor"
    )

    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
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
