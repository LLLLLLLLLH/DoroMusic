package com.doro.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doro.music.data.model.DisplayMode

@Composable
fun DisplayItem(
    modifier: Modifier = Modifier,
    mode: DisplayMode,
    title: String,
    subtitle: String,
    albumArt: String? = null,
    placeholderIcon: ImageVector = Icons.Rounded.AudioFile,
    menu: @Composable (OptionMenuScope.() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    when (mode) {
        DisplayMode.GRID -> GridItem(modifier, title, subtitle, albumArt, placeholderIcon, onClick)
        DisplayMode.LIST -> ListItem(modifier, title, subtitle, albumArt, placeholderIcon, menu, onClick)
        DisplayMode.COMPACT -> CompactItem(modifier, title, subtitle, albumArt, placeholderIcon, menu, onClick)
    }
}

@Composable
private fun CompactItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    albumArt: String?,
    placeholderIcon: ImageVector = Icons.Rounded.AudioFile,
    menu: @Composable (OptionMenuScope.() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkImage(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            imageUrl = albumArt,
            placeholderIcon = placeholderIcon
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(1.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        MoreMenuButton(content = menu)
    }
}

@Composable
private fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    albumArt: String?,
    placeholderIcon: ImageVector = Icons.Rounded.AudioFile,
    menu: @Composable (OptionMenuScope.() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ArtworkImage(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            imageUrl = albumArt,
            placeholderIcon = placeholderIcon
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        MoreMenuButton(content = menu)
    }
}

@Composable
private fun GridItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    albumArt: String?,
    placeholderIcon: ImageVector = Icons.Rounded.AudioFile,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArtworkImage(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            imageUrl = albumArt,
            placeholderIcon = placeholderIcon
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MoreMenuButton(content: @Composable (OptionMenuScope.() -> Unit)? = null) {
    if (content == null) return

    var showMore by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMore = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = null)
        }
        OptionMenu(
            expanded = showMore,
            onDismissRequest = { showMore = false },
            content = content
        )
    }
}
