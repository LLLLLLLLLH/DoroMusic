package com.doro.music.ui.component.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.doro.music.R
import com.doro.music.player.model.QueueSong
import com.doro.music.ui.component.ArtworkImage

@Composable
fun PlayQueue(
    modifier: Modifier = Modifier,
    queueItems: LazyPagingItems<QueueSong>,
    currentQueueId: Long?,
    onSongClick: (Long) -> Unit = {},
    onRemove: (Long) -> Unit = {}
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentQueueId) {
        val targetIndex = queueItems.itemSnapshotList.items.indexOfFirst { it.queueId == currentQueueId }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.play_queue_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            val totalCount = queueItems.itemCount
            val currentPos = queueItems.itemSnapshotList.items.indexOfFirst { it.queueId == currentQueueId }
            Text(
                text = stringResource(R.string.play_queue_count, currentPos + 1, totalCount),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        LazyColumn(state = listState) {
            items(
                count = queueItems.itemCount,
                key = queueItems.itemKey { it.queueId }
            ) { index ->
                val queueSong = queueItems[index] ?: return@items
                PlayQueueItem(
                    song = queueSong,
                    isActive = queueSong.queueId == currentQueueId,
                    onClick = { onSongClick(queueSong.queueId) },
                    onRemove = { onRemove(queueSong.queueId) }
                )
            }
        }
    }
}

@Composable
private fun PlayQueueItem(
    modifier: Modifier = Modifier,
    song: QueueSong,
    isActive: Boolean,
    onClick: () -> Unit = {},
    onRemove: () -> Unit = {}
) {
    val contentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkImage(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            imageUrl = song.song.albumArt,
            placeholderIcon = Icons.Rounded.MusicNote
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.song.title,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = song.song.artist.orEmpty(),
                fontSize = 12.sp,
                color = if (isActive) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.remove_from_queue),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}