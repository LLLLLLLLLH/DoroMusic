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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
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
    val progress by remember(position, duration) {
        derivedStateOf { if (duration > 0) position.toFloat() / duration else 0f }
    }

    var isInteracting by remember { mutableStateOf(false) }

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = PlayerSliderDefaults.INACTIVE_TRACK_ALPHA)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.bodySmall

    val posLabel = remember(position) { position.formatDuration() }
    val durLabel = remember(duration) { duration.formatDuration() }

    Column(
        modifier = modifier.padding(
            horizontal = PlayerSliderDefaults.HORIZONTAL_PADDING,
            vertical = PlayerSliderDefaults.VERTICAL_PADDING
        )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(PlayerSliderDefaults.TOUCH_AREA_HEIGHT)
                .pointerInput(duration, onSeek) { // 将 Key 传入，确保回调正确
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        isInteracting = true

                        // 封装 seek 逻辑
                        fun doSeek(x: Float) {
                            val p = (x / size.width).coerceIn(0f, 1f)
                            onSeek((p * duration).toLong())
                        }

                        doSeek(down.position.x)
                        drag(down.id) { change ->
                            doSeek(change.position.x)
                            change.consume()
                        }
                        isInteracting = false
                    }
                }
        ) {
            val trackHeight = if (isInteracting) PlayerSliderDefaults.TRACK_HEIGHT_ACTIVE else PlayerSliderDefaults.TRACK_HEIGHT_INACTIVE
            val thumbRadius = if (isInteracting) PlayerSliderDefaults.THUMB_RADIUS_ACTIVE else PlayerSliderDefaults.THUMB_RADIUS_INACTIVE

            val trackPx = trackHeight.toPx()
            val thumbPx = thumbRadius.toPx()

            val cy = size.height / 2
            val y = cy - trackPx / 2
            val currentProgressWidth = size.width * progress

            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(0f, y),
                size = Size(size.width, trackPx),
                cornerRadius = CornerRadius(trackPx / 2),
            )
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(0f, y),
                size = Size(currentProgressWidth, trackPx),
                cornerRadius = CornerRadius(trackPx / 2),
            )
            drawCircle(
                color = activeColor,
                radius = thumbPx,
                center = Offset(currentProgressWidth, cy)
            )
        }

        Row(Modifier
            .fillMaxWidth()
            .padding(top = PlayerSliderDefaults.TIME_LABEL_TOP_MARGIN)) {
            Text(posLabel, style = labelStyle, color = labelColor)
            Spacer(Modifier.weight(1f))
            Text(durLabel, style = labelStyle, color = labelColor)
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
