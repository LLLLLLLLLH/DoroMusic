package com.doro.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import coil3.compose.AsyncImage

@Composable
fun ArtworkImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    placeholderIcon: ImageVector = Icons.Rounded.AudioFile,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl == null) {
            Icon(
                imageVector = placeholderIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            AsyncImage(
                model = imageUrl.toUri(),
                contentDescription = null,
                error = placeholderPainter(),
                fallback = placeholderPainter(),
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}
