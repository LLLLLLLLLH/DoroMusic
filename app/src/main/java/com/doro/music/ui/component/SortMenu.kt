package com.doro.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.doro.music.R
import com.doro.music.data.model.DisplayMode
import com.doro.music.data.model.SortMode

@Composable
fun SortMenu(
    sortOptions: List<SortMode>,
    sortBy: SortMode,
    displayMode: DisplayMode,
    onSortChange: (SortMode) -> Unit,
    onDisplayModeChange: (DisplayMode) -> Unit,
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Box {
        IconAction(
            imageVector = Icons.Rounded.FilterList,
            contentDescription = stringResource(R.string.filter_sort),
            onClick = { showMoreMenu = true }
        )
        OptionMenu(
            expanded = showMoreMenu,
            onDismissRequest = { showMoreMenu = false }
        ) {
            SingleChoiceGroup(currentValue = sortBy, onValueChanged = onSortChange) {
                sortOptions.forEach { mode ->
                    Choice(mode, stringResource(mode.labelResId))
                }
            }
            OptionGroup(
                groupLabel = { Text(stringResource(R.string.display_mode)) },
                content = {
                    SingleChoiceGroup(currentValue = displayMode, onValueChanged = onDisplayModeChange) {
                        Choice(DisplayMode.COMPACT, stringResource(DisplayMode.COMPACT.labelResId))
                        Choice(DisplayMode.LIST, stringResource(DisplayMode.LIST.labelResId))
                        Choice(DisplayMode.GRID, stringResource(DisplayMode.GRID.labelResId))
                    }
                })
        }
    }
}
