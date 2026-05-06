package com.doro.music.ui.screen

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.doro.music.ui.screen.main.MainRoute
import com.doro.music.ui.screen.main.mainRoute
import com.doro.music.ui.screen.other.songDetailRoute
import com.doro.music.ui.screen.search.searchRoute
import com.doro.music.ui.screen.settings.settingsRoute

@Composable
fun AppNav(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(MainRoute)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(rememberViewModelStoreNavEntryDecorator()),
        transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
        popTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
        predictivePopTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
        entryProvider = entryProvider {
            mainRoute(backStack)
            searchRoute(backStack)
            settingsRoute(backStack)
            songDetailRoute(backStack)
        }
    )
}
