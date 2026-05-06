package com.doro.music.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.doro.music.R
import com.doro.music.data.model.Folder
import com.doro.music.data.model.excludedFolders
import com.doro.music.ui.component.SettingsScaffold
import com.doro.music.vm.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

private val DIALOG_MAX_HEIGHT = 300.dp
private const val DURATION_MAX_SECONDS = 60
private const val DURATION_STEP_SIZE = 10
private const val DURATION_SLIDER_STEPS = (DURATION_MAX_SECONDS / DURATION_STEP_SIZE) - 1

fun EntryProviderScope<NavKey>.scanSettingsEntry(onBack: () -> Unit) {
    entry<SettingsNavKey.Scan> {
        ScanSettingsScreen(onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSettingsScreen(
    vm: SettingsViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    var showDurationDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    val settings by vm.settings.collectAsStateWithLifecycle()
    val folders by vm.folders.collectAsStateWithLifecycle()

    val excludedFoldersCount = folders.excludedFolders().size

    SettingsScaffold(
        title = stringResource(R.string.scan_settings),
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                modifier = Modifier.clickable { showDurationDialog = true },
                headlineContent = { Text(stringResource(R.string.min_duration_filter)) },
                supportingContent = {
                    Text(
                        if (settings.minDurationFilter == 0) stringResource(R.string.no_filter)
                        else stringResource(R.string.seconds_value, settings.minDurationFilter)
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable { showFolderDialog = true },
                headlineContent = { Text(stringResource(R.string.excluded_folders)) },
                supportingContent = {
                    Text(
                        if (excludedFoldersCount == 0) stringResource(R.string.no_excluded_folders)
                        else stringResource(R.string.excluded_folders_count, excludedFoldersCount)
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
    }

    if (showDurationDialog) {
        DurationFilterDialog(
            current = settings.minDurationFilter,
            onConfirm = {
                vm.setMinDurationFilter(it)
                showDurationDialog = false
            },
            onDismiss = { showDurationDialog = false }
        )
    }

    if (showFolderDialog) {
        ExcludedFoldersDialog(
            folders = folders,
            onConfirm = {
                vm.setExcludedFolders(it)
                showFolderDialog = false
            },
            onDismiss = { showFolderDialog = false }
        )
    }
}

@Composable
private fun DurationFilterDialog(
    current: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderValue by remember { mutableIntStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.min_duration_filter)) },
        text = {
            Column {
                Text(
                    text = if (sliderValue == 0) stringResource(R.string.no_filter)
                    else stringResource(R.string.seconds_value, sliderValue),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderValue.toFloat(),
                    onValueChange = { sliderValue = it.toInt() },
                    valueRange = 0f..DURATION_MAX_SECONDS.toFloat(),
                    steps = DURATION_SLIDER_STEPS,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelCount = DURATION_MAX_SECONDS / DURATION_STEP_SIZE + 1
                    repeat(labelCount) { step ->
                        Text(
                            text = "${step * DURATION_STEP_SIZE}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sliderValue) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ExcludedFoldersDialog(
    folders: List<Folder>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val excluded = folders.excludedFolders().map { it.path }.toSet()
    var selected by remember(folders) { mutableStateOf(excluded) }


    val toggleSelection: (String) -> Unit = { path ->
        selected = if (path in selected) selected - path else selected + path
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.excluded_folders)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DIALOG_MAX_HEIGHT)
                    .verticalScroll(rememberScrollState())
            ) {
                if (folders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_folders),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    folders.forEach { folder ->
                        val isSelected = folder.path in selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toggleSelection(folder.path) }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { toggleSelection(folder.path) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = folder.path,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toList()) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
