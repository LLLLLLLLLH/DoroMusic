package com.doro.music.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset

sealed class Anchor {
    data class Pointer(
        val positionInWindow: Offset = Offset.Zero,
        val anchorBoundsInWindow: Rect = Rect.Zero,
        val offset: DpOffset = DpOffset.Zero
    ) : Anchor()
}

@Composable
fun AnchorOverlay(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    anchor: Anchor = Anchor.Pointer(),
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current

    val menuOffset = when (anchor) {
        is Anchor.Pointer -> with(density) {
            DpOffset(
                x = (anchor.positionInWindow.x - anchor.anchorBoundsInWindow.left).toDp() + anchor.offset.x,
                y = (anchor.positionInWindow.y - anchor.anchorBoundsInWindow.bottom).toDp() + anchor.offset.y
            )
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = menuOffset,
        modifier = modifier,
        content = content
    )
}

fun Modifier.clickableAnchor(
    enabled: Boolean = true,
    onClick: (Anchor.Pointer) -> Unit
): Modifier = composed(
    inspectorInfo = {
        name = "clickableAnchor"
        properties["enabled"] = enabled
        properties["onClick"] = onClick
    }
) {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)

    this
        .onGloballyPositioned { coordinates = it }
        .indication(interactionSource, if (enabled) LocalIndication.current else null)
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = { offset ->
                    val coords = coordinates
                    if (coords != null) {
                        val windowOffset = coords.localToWindow(offset)
                        val bounds = coords.boundsInWindow()

                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)

                        val released = tryAwaitRelease()

                        val end = if (released) PressInteraction.Release(press)
                        else PressInteraction.Cancel(press)
                        interactionSource.emit(end)

                        if (released) {
                            currentOnClick(
                                Anchor.Pointer(
                                    positionInWindow = windowOffset,
                                    anchorBoundsInWindow = bounds
                                )
                            )
                        }
                    }
                }
            )
        }
}
