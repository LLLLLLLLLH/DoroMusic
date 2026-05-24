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
import com.doro.music.data.model.DarkThemeMode
import com.doro.music.ui.component.SettingsScaffold
import com.doro.music.vm.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun EntryProviderScope<NavKey>.displaySettingsEntry(onBack: () -> Unit) {
    entry<SettingsNavKey.Display> {
        DisplaySettingsRoute(onBack = onBack)
    }
}

@Composable
fun DisplaySettingsRoute(
    vm: SettingsViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    DisplaySettingsScreen(
        darkTheme = settings.darkTheme,
        onDarkThemeChange = vm::setDarkTheme,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplaySettingsScreen(
    darkTheme: DarkThemeMode,
    onDarkThemeChange: (DarkThemeMode) -> Unit,
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.display_settings),
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
                    headlineContent = { Text(stringResource(R.string.dark_theme)) },
                    supportingContent = { Text(darkTheme.toDisplayName()) },
                    trailingContent = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DarkThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.toDisplayName()) },
                            onClick = {
                                onDarkThemeChange(mode)
                                expanded = false
                            },
                            trailingIcon = {
                                if (mode == darkTheme) {
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
private fun DarkThemeMode.toDisplayName(): String = stringResource(
    when (this) {
        DarkThemeMode.SYSTEM -> R.string.dark_theme_system
        DarkThemeMode.LIGHT -> R.string.dark_theme_light
        DarkThemeMode.DARK -> R.string.dark_theme_dark
    }
)
