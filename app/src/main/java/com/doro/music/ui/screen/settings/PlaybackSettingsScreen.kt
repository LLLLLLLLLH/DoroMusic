package com.doro.music.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.doro.music.R
import com.doro.music.data.model.PlayMode
import com.doro.music.ui.component.SettingsScaffold
import com.doro.music.vm.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.playbackSettingsEntry(onBack: () -> Unit) {
    entry<SettingsNavKey.Playback> {
        PlaybackSettingsRoute(onBack = onBack)
    }
}

@Composable
fun PlaybackSettingsRoute(
    vm: SettingsViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    PlaybackSettingsScreen(
        defaultPlayMode = settings.defaultPlayMode,
        onPlayModeChange = vm::setPlayMode,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackSettingsScreen(
    defaultPlayMode: PlayMode,
    onPlayModeChange: (PlayMode) -> Unit,
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.playback_settings),
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                ListItem(
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    headlineContent = { Text(stringResource(R.string.default_play_mode)) },
                    supportingContent = { Text(defaultPlayMode.toDisplayName()) },
                    trailingContent = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    PlayMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.toDisplayName()) },
                            onClick = {
                                onPlayModeChange(mode)
                                expanded = false
                            },
                            trailingIcon = {
                                if (mode == defaultPlayMode) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayMode.toDisplayName(): String = stringResource(when (this) {
    PlayMode.REPEAT -> R.string.play_mode_repeat
    PlayMode.SHUFFLE -> R.string.play_mode_shuffle
    PlayMode.REPEAT_ONE -> R.string.play_mode_repeat_one
})