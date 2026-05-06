package com.doro.music.ui.component.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.doro.music.ext.formatDuration

private object PlayerSliderDefaults {
    val TRACK_HEIGHT_ACTIVE = 8.dp
    val TRACK_HEIGHT_INACTIVE = 4.dp
    val THUMB_RADIUS_ACTIVE = 7.dp
    val THUMB_RADIUS_INACTIVE = 4.dp
    val TOUCH_AREA_HEIGHT = 10.dp
    val HORIZONTAL_PADDING = 16.dp
    val VERTICAL_PADDING = 20.dp
    val TIME_LABEL_TOP_MARGIN = 10.dp
    const val SLIDER_TOP_MARGIN = 8f
    const val INACTIVE_TRACK_ALPHA = 0.24f
}

@Composable
fun PlayerSlider(
    modifier: Modifier = Modifier,
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit = {}
) {
    val progress = if (duration > 0) (position.toDouble() / duration).toFloat() else 0f
    var isInteracting by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val trackHeight = if (isInteracting) PlayerSliderDefaults.TRACK_HEIGHT_ACTIVE else PlayerSliderDefaults.TRACK_HEIGHT_INACTIVE
    val thumbRadius = if (isInteracting) PlayerSliderDefaults.THUMB_RADIUS_ACTIVE else PlayerSliderDefaults.THUMB_RADIUS_INACTIVE
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = PlayerSliderDefaults.INACTIVE_TRACK_ALPHA)

    val trackPx = remember(density, isInteracting) { with(density) { trackHeight.toPx() } }
    val thumbPx = remember(density, isInteracting) { with(density) { thumbRadius.toPx() } }

    val seekTo = remember(onSeek, duration) { { x: Float, width: Int ->
        val p = (x / width).coerceIn(0f, 1f)
        onSeek((p * duration).toLong())
    }}

    Column(modifier = modifier.padding(horizontal = PlayerSliderDefaults.HORIZONTAL_PADDING, vertical = PlayerSliderDefaults.VERTICAL_PADDING)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(PlayerSliderDefaults.TOUCH_AREA_HEIGHT)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        seekTo(down.position.x, size.width)
                        isInteracting = true
                        drag(down.id) { change ->
                            val x = change.position.x.coerceIn(0f, size.width.toFloat())
                            seekTo(x, size.width)
                            change.consume()
                        }
                        isInteracting = false
                    }
                }
        ) {
            val cy = size.height / 2
            val y = cy - trackPx / 2

            drawRoundRect(inactiveColor, Offset(0f, y), Size(size.width, trackPx), CornerRadius(trackPx / 2))
            drawRoundRect(activeColor, Offset(0f, y), Size(size.width * progress, trackPx), CornerRadius(trackPx / 2))
            drawCircle(activeColor, thumbPx, Offset(size.width * progress, cy))
        }
        Row(Modifier.fillMaxWidth().padding(top = PlayerSliderDefaults.TIME_LABEL_TOP_MARGIN)) {
            Text(position.formatDuration(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(duration.formatDuration(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun PlayerSliderPreview() {
    var position by remember { mutableLongStateOf(0) }
    PlayerSlider(position = position, duration = 0, onSeek = { position = it })
}
