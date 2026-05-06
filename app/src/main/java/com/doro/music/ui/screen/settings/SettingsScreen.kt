package com.doro.music.ui.screen.settings

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.doro.music.R
import com.doro.music.ext.safePop
import com.doro.music.ui.component.SettingsScaffold
import com.doro.music.ui.theme.DoroMusicTheme
import kotlinx.serialization.Serializable

@Serializable
data object Settings : NavKey

fun EntryProviderScope<NavKey>.settingsRoute(backStack: NavBackStack<NavKey>) {
    entry<Settings> {
        SettingsScreen(onBack = backStack::safePop)
    }
}

@Serializable
sealed class SettingsNavKey : NavKey {
    @Serializable
    data object Menu : SettingsNavKey()

    @Serializable
    data object Playback : SettingsNavKey()

    @Serializable
    data object Display : SettingsNavKey()

    @Serializable
    data object Scan : SettingsNavKey()

    @Serializable
    data object About : SettingsNavKey()
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val backStack = rememberNavBackStack(SettingsNavKey.Menu)

    NavDisplay(
        backStack = backStack,
        transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
        popTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
        predictivePopTransitionSpec = { slideInHorizontally { -it } togetherWith slideOutHorizontally { it } },
        entryProvider = entryProvider {
            entry<SettingsNavKey.Menu> {
                SettingsMenuScreen(
                    onBack = onBack,
                    onNavigate = { backStack.add(it) }
                )
            }
            playbackSettingsEntry(onBack = backStack::safePop)
            displaySettingsEntry(onBack = backStack::safePop)
            scanSettingsEntry(onBack = backStack::safePop)
            aboutSettingsEntry(onBack = backStack::safePop)
        }
    )
}

@Composable
private fun SettingsMenuScreen(
    onBack: () -> Unit,
    onNavigate: (SettingsNavKey) -> Unit
) {
    SettingsScaffold(
        title = stringResource(R.string.settings),
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategory(
                title = stringResource(R.string.playback_settings),
                icon = Icons.Rounded.MusicNote,
                subtitle = stringResource(R.string.playback_settings_desc),
                onClick = { onNavigate(SettingsNavKey.Playback) }
            )
            SettingsCategory(
                title = stringResource(R.string.display_settings),
                icon = Icons.Rounded.ColorLens,
                subtitle = stringResource(R.string.display_settings_desc),
                onClick = { onNavigate(SettingsNavKey.Display) }
            )
            SettingsCategory(
                title = stringResource(R.string.scan_settings),
                icon = Icons.Rounded.Search,
                subtitle = stringResource(R.string.scan_settings_desc),
                onClick = { onNavigate(SettingsNavKey.Scan) }
            )
            SettingsCategory(
                title = stringResource(R.string.about_app),
                icon = Icons.Rounded.Info,
                subtitle = null,
                onClick = { onNavigate(SettingsNavKey.About) }
            )
        }
    }
}

@Composable
private fun SettingsCategory(
    title: String,
    icon: ImageVector,
    subtitle: String?,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsMenuScreenPreview() {
    DoroMusicTheme {
        SettingsMenuScreen(onBack = {}, onNavigate = {})
    }
}
