@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import com.doro.music.data.model.PlayerAction
import com.doro.music.data.model.Song
import com.doro.music.ext.safePop
import com.doro.music.ui.component.player.PlayQueue
import com.doro.music.ui.component.player.PlayerSheetContent
import com.doro.music.ui.component.player.PlayerSheetState
import com.doro.music.ui.component.rememberSheetProgressState
import com.doro.music.ui.screen.other.SongDetail
import com.doro.music.ui.screen.other.SongList
import com.doro.music.ui.screen.other.SongListSource
import com.doro.music.ui.screen.other.songListScreen
import com.doro.music.ui.screen.search.Search
import com.doro.music.ui.screen.settings.Settings
import com.doro.music.vm.PlayerViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object MainRoute : NavKey

fun EntryProviderScope<NavKey>.mainRoute(backStack: NavBackStack<NavKey>) {
    entry<MainRoute> {
        val activity = LocalActivity.current
        MainRoute(
            onBack = { activity?.finish() },
            onSearchClick = { backStack.add(Search) },
            onSettingsClick = { backStack.add(Settings) },
            onSongDetailClick = { backStack.add(SongDetail(it)) }
        )
    }
}

@Composable
private fun MainRoute(
    vm: PlayerViewModel = koinViewModel(),
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSongDetailClick: (song: Song) -> Unit
) {
    val backStack = rememberNavBackStack(Main)

    // MainRoute 只收集影响自身布局的状态
    val playerSheetState by vm.playerSheetState.collectAsStateWithLifecycle()
    val isQueueVisible by vm.isQueueVisible.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val playQueueState = rememberModalBottomSheetState()

    // P2: peekHeight 动画过渡
    val peekHeight by animateDpAsState(
        targetValue = when (playerSheetState) {
            PlayerSheetState.Hidden -> 0.dp
            else -> 78.dp
        },
        animationSpec = tween(300),
        label = "peekHeight"
    )

    val sheetProgressState = rememberSheetProgressState(
        scaffoldState = bottomSheetScaffoldState,
        peekHeight = peekHeight
    )

    // VM -> Scaffold: 驱动 Sheet 展开/收起
    LaunchedEffect(playerSheetState) {
        when (playerSheetState) {
            PlayerSheetState.Expanded -> bottomSheetScaffoldState.bottomSheetState.expand()
            else -> {
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                vm.restorePlayerView()
            }
        }
    }

    // P1: Scaffold -> VM: 手势拖拽后同步状态回 ViewModel
    LaunchedEffect(bottomSheetScaffoldState) {
        snapshotFlow { bottomSheetScaffoldState.bottomSheetState.currentValue }
            .distinctUntilChanged()
            .collect { sheetValue ->
                when {
                    sheetValue == SheetValue.Expanded &&
                        playerSheetState != PlayerSheetState.Expanded -> {
                        vm.syncSheetState(PlayerSheetState.Expanded)
                    }
                    sheetValue == SheetValue.PartiallyExpanded &&
                        playerSheetState == PlayerSheetState.Expanded -> {
                        vm.syncSheetState(PlayerSheetState.Collapsed)
                    }
                }
            }
    }

    LaunchedEffect(isQueueVisible) {
        if (isQueueVisible) playQueueState.expand() else playQueueState.partialExpand()
    }

    BackHandler {
        if (!vm.handleBack()) onBack()
    }

    BottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets()),
        scaffoldState = bottomSheetScaffoldState,
        sheetShape = RectangleShape,
        sheetContent = {
            PlayerSheetContent(
                vm = vm,
                sheetProgressState = sheetProgressState,
                modifier = Modifier
            )
        },
        sheetPeekHeight = sheetProgressState.peekHeight,
        sheetMaxWidth = Dp.Unspecified,
        sheetDragHandle = null
    ) { innerPaddingValues ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)
                .consumeWindowInsets(innerPaddingValues),
            backStack = backStack,
            transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
            popTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
            predictivePopTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
            entryProvider = entryProvider {
                mainScreen(
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick,
                    onSongDetailClick = onSongDetailClick,
                    onArtistClick = { backStack.add(SongList(SongListSource.FromArtist(it))) },
                    onPlaylistClick = { backStack.add(SongList(SongListSource.FromPlaylist(it))) },
                )
                songListScreen(
                    onBack = backStack::safePop,
                    onSongDetailClick = onSongDetailClick
                )
            }
        )
    }

    if (isQueueVisible && playerSheetState != PlayerSheetState.Hidden) {
        ModalBottomSheet(
            sheetState = playQueueState,
            shape = RoundedCornerShape(10.dp),
            onDismissRequest = { vm.handlePlayerAction(PlayerAction.TogglePlayQueue) }
        ) {
            val queueItems = vm.playQueuePaging.collectAsLazyPagingItems()
            PlayQueue(
                queueItems = queueItems,
                currentQueueId = uiState.currentQueueId,
                onSongClick = vm::seekToQueueItem,
                onRemove = vm::removeFromPlayQueue
            )
        }
    }
}
