@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.component

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

@Stable
class SheetProgressState(
    val peekHeight: Dp,
    private val progressState: State<Float>
) {
    val progress: Float get() = progressState.value
}

@Composable
fun rememberSheetProgressState(
    scaffoldState: BottomSheetScaffoldState,
    peekHeight: Dp
): SheetProgressState {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    val progressState = remember(density, windowInfo, peekHeight) {
        derivedStateOf {
            val sheetOffset = try {
                scaffoldState.bottomSheetState.requireOffset()
            } catch (_: IllegalStateException) {
                0f
            }
            val peekHeightPx = with(density) { peekHeight.toPx() }
            val screenHeightPx = with(density) { windowInfo.containerDpSize.height.toPx() }
            val totalDragRange = screenHeightPx - peekHeightPx
            1f - (sheetOffset / totalDragRange).coerceIn(0f, 1f)
        }
    }

    return remember(peekHeight) { SheetProgressState(peekHeight = peekHeight, progressState = progressState) }
}
