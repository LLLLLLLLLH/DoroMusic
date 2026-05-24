package com.doro.music.ui.component.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.dp
import com.doro.music.data.model.PlaybackState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private object VinylDiscDefaults {
    const val ROTATION_DURATION_MS = 8000
    const val ROTATION_DEGREES = 360f
    val BORDER_WIDTH_DP = 10.dp
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

    const val INSPECTOR_NAME = "vinylRotation"
    const val PROPERTY_PLAYBACK_STATE = "playbackState"
    const val PROPERTY_KEY = "key"
}

data class VinylRotationElement(
    val playbackState: PlaybackState,
    val key: Any
) : ModifierNodeElement<VinylRotationNode>() {
    override fun create() = VinylRotationNode(playbackState, key)

    override fun update(node: VinylRotationNode) {
        node.update(playbackState, key)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = VinylDiscDefaults.INSPECTOR_NAME
        properties[VinylDiscDefaults.PROPERTY_PLAYBACK_STATE] = playbackState
        properties[VinylDiscDefaults.PROPERTY_KEY] = key
    }
}

class VinylRotationNode(
    private var playbackState: PlaybackState,
    private var key: Any
) : Modifier.Node(), DrawModifierNode {

    private val rotation = Animatable(0f)
    private var animationJob: Job? = null

    override fun onAttach() {
        observePlayback()
    }

    fun update(newPlaybackState: PlaybackState, newKey: Any) {
        val keyChanged = this.key != newKey
        val stateChanged = this.playbackState != newPlaybackState

        if (keyChanged || stateChanged) {
            this.key = newKey
            this.playbackState = newPlaybackState

            if (keyChanged) {
                coroutineScope.launch { rotation.snapTo(0f) }
            }
            observePlayback()
        }
    }

    private fun observePlayback() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            if (playbackState == PlaybackState.PLAYING) {
                rotation.animateTo(
                    targetValue = rotation.value + VinylDiscDefaults.ROTATION_DEGREES,
                    animationSpec = infiniteRepeatable(
                        animation = tween(VinylDiscDefaults.ROTATION_DURATION_MS, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                ) {
                    invalidateDraw()
                }
            } else if (rotation.isRunning) {
                rotation.animateTo(
                    targetValue = rotation.value + VinylDiscDefaults.DECAL_EXTRA_DEGREES,
                    animationSpec = tween(
                        durationMillis = VinylDiscDefaults.DECAL_DURATION_MS,
                        easing = FastOutSlowInEasing
                    )
                ) {
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        withTransform({
            rotate(rotation.value)
        }) {
            this@draw.drawContent()
        }
    }
}

private fun Modifier.vinylRotation(playbackState: PlaybackState, key: Any) =
    this.then(VinylRotationElement(playbackState, key))

@Composable
fun VinylDisc(
    modifier: Modifier = Modifier,
    playbackState: PlaybackState,
    key: () -> Any = { },
    centerContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1F)
            .vinylRotation(playbackState, key),
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
                    .padding(VinylDiscDefaults.BORDER_WIDTH_DP)
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
                    .padding(VinylDiscDefaults.BORDER_WIDTH_DP)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                centerContent()
            }
        }
    }
}
