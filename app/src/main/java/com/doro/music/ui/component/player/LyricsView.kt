package com.doro.music.ui.component.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.LyricsLine
import kotlinx.coroutines.delay

@Composable
fun LyricsView(
    lyrics: LyricsData?,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    onSeekToLine: ((Long) -> Unit)? = null
) {
    if (lyrics == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "暂无歌词",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    SyncedLyricsContent(
        lines = lyrics.lines,
        currentIndex = currentIndex,
        modifier = modifier,
        onLineClick = onSeekToLine
    )
}

private const val SYNC_RESUME_TIMEOUT_MS = 10_000L
private const val TARGET_POSITION_RATIO = 0.33F

@Composable
private fun SyncedLyricsContent(
    lines: List<LyricsLine>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    onLineClick: ((Long) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    var userScrolling by remember { mutableStateOf(false) }

    LaunchedEffect(isDragged) {
        if (isDragged) {
            userScrolling = true
        } else {
            if (userScrolling) {
                delay(SYNC_RESUME_TIMEOUT_MS)
                userScrolling = false
            }
        }
    }

    val overscrollInterceptConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput) return available
                return Offset.Zero
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerHeightDp = maxHeight
        val containerHeightPx = with(density) { containerHeightDp.toPx() }
        val bottomPaddingDp = containerHeightDp * (1f - TARGET_POSITION_RATIO)

        LaunchedEffect(currentIndex, userScrolling) {
            if (userScrolling || lines.isEmpty() || currentIndex !in lines.indices) {
                return@LaunchedEffect
            }

            val targetOffset = -(containerHeightPx * TARGET_POSITION_RATIO).toInt()
            listState.animateScrollToItem(index = currentIndex, scrollOffset = targetOffset)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(overscrollInterceptConnection),
            contentPadding = PaddingValues(bottom = bottomPaddingDp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                LyricLineItem(
                    text = line.text,
                    isActive = index == currentIndex,
                    isPast = index < currentIndex,
                    onClick = {
                        if (line.timeMs >= 0) {
                            userScrolling = false
                            onLineClick?.invoke(line.timeMs)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LyricLineItem(
    text: String,
    isActive: Boolean,
    isPast: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = when {
            isActive -> 1f
            isPast -> 0.5f
            else -> 0.6f
        },
        animationSpec = tween(300),
        label = "lyricAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "lyricScale"
    )

    Text(
        text = text,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clickable { onClick() },
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = if (isActive) 18.sp else 16.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        ),
        color = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}