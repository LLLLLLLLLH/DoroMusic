package com.doro.music.ui.component.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.doro.music.data.model.PlaybackState

private object VinylDiscDefaults {
    const val ROTATION_DURATION_MS = 8000
    const val ROTATION_DEGREES = 360f
    const val BORDER_WIDTH_DP = 10
    const val GROOVE_COUNT = 20
    const val DECAL_EXTRA_DEGREES = 30f
    const val DECAL_DURATION_MS = 1500

    val OUTER_COLOR = Color(0xFF111111)
    val DISC_GRADIENT_COLORS = listOf(Color(0xFF2A2A2A), Color(0xFF181818))
    val GROOVE_COLOR = Color(0x15000000)
    const val GROOVE_WIDTH = 0.5f
    const val GROOVE_INNER_RATIO = 0.35f
    const val GROOVE_RANGE_RATIO = 0.58f
    const val GRADIENT_RADIUS_RATIO = 0.95f
}

private fun Modifier.vinylRotation(playbackState: PlaybackState,key: () -> Any) = composed {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(key) {
        rotation.snapTo(0F)
    }

    LaunchedEffect(playbackState) {
        when (playbackState) {
            PlaybackState.PLAYING -> {
                while (true) {
                    val start = rotation.value
                    rotation.animateTo(
                        start + VinylDiscDefaults.ROTATION_DEGREES,
                        tween(VinylDiscDefaults.ROTATION_DURATION_MS, easing = LinearEasing)
                    )
                    rotation.snapTo(start % VinylDiscDefaults.ROTATION_DEGREES)
                }
            }

            else -> {
                if (rotation.isRunning) {
                    rotation.animateTo(
                        rotation.value + VinylDiscDefaults.DECAL_EXTRA_DEGREES,
                        tween(VinylDiscDefaults.DECAL_DURATION_MS, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    this then Modifier.graphicsLayer { rotationZ = rotation.value }
}

@Composable
fun VinylDisc(
    modifier: Modifier = Modifier,
    playbackState: PlaybackState,
    key: () -> Any = { },
    centerContent: @Composable () -> Unit
) {
    val borderWidth = VinylDiscDefaults.BORDER_WIDTH_DP.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1F)
            .vinylRotation(playbackState,key),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(borderWidth)
                    .clip(CircleShape)
                    .background(VinylDiscDefaults.OUTER_COLOR)
            ) {
                val radius = size.minDimension / 2
                drawCircle(
                    Brush.radialGradient(
                        colors = VinylDiscDefaults.DISC_GRADIENT_COLORS,
                        radius = radius * VinylDiscDefaults.GRADIENT_RADIUS_RATIO
                    )
                )
                repeat(VinylDiscDefaults.GROOVE_COUNT) { i ->
                    drawCircle(
                        color = VinylDiscDefaults.GROOVE_COLOR,
                        radius = radius * (VinylDiscDefaults.GROOVE_INNER_RATIO + (i.toFloat() / VinylDiscDefaults.GROOVE_COUNT) * VinylDiscDefaults.GROOVE_RANGE_RATIO),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = VinylDiscDefaults.GROOVE_WIDTH)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(borderWidth)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                centerContent()
            }
        }
    }
}
