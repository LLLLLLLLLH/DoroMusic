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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doro.music.data.model.DisplayMode

private object DisplayItemDefaults {
    val CompactImageSize = 40.dp
    val ListImageSize = 48.dp
    val ImageCorner = 8.dp
    val HorizontalPadding = 20.dp
    val InternalSpacing = 12.dp
    val GridPadding = 4.dp
    val GridTextTopMargin = 6.dp

    val ListVerticalPadding = 8.dp
    val CompactVerticalPadding = 4.dp

    val SpacingHigh = 2.dp
    val SpacingLow = 1.dp

    val TitleSizeLarge = 15.sp
    val TitleSizeNormal = 14.sp
    val SubtitleSizeLarge = 13.sp
    val SubtitleSizeNormal = 12.sp
}

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
    val commonModifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)

    when (mode) {
        DisplayMode.GRID -> GridItem(
            modifier = commonModifier,
            title = title,
            subtitle = subtitle,
            albumArt = albumArt,
            placeholderIcon = placeholderIcon
        )

        DisplayMode.LIST -> HorizontalItem(
            modifier = commonModifier,
            title = title,
            subtitle = subtitle,
            albumArt = albumArt,
            imageSize = DisplayItemDefaults.ListImageSize,
            verticalPadding = DisplayItemDefaults.ListVerticalPadding,
            titleSize = DisplayItemDefaults.TitleSizeLarge,
            subtitleSize = DisplayItemDefaults.SubtitleSizeLarge,
            spacing = DisplayItemDefaults.SpacingHigh,
            placeholderIcon = placeholderIcon,
            menu = menu
        )

        DisplayMode.COMPACT -> HorizontalItem(
            modifier = commonModifier,
            title = title,
            subtitle = subtitle,
            albumArt = albumArt,
            imageSize = DisplayItemDefaults.CompactImageSize,
            verticalPadding = DisplayItemDefaults.CompactVerticalPadding,
            titleSize = DisplayItemDefaults.TitleSizeNormal,
            subtitleSize = DisplayItemDefaults.SubtitleSizeNormal,
            spacing = DisplayItemDefaults.SpacingLow,
            placeholderIcon = placeholderIcon,
            menu = menu
        )
    }
}

@Composable
private fun HorizontalItem(
    modifier: Modifier,
    title: String,
    subtitle: String,
    albumArt: String?,
    imageSize: Dp,
    verticalPadding: Dp,
    titleSize: TextUnit,
    subtitleSize: TextUnit,
    spacing: Dp,
    placeholderIcon: ImageVector,
    menu: @Composable (OptionMenuScope.() -> Unit)?
) {
    Row(
        modifier = modifier.padding(
            vertical = verticalPadding,
            horizontal = DisplayItemDefaults.HorizontalPadding
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemImage(
            modifier = Modifier.size(imageSize),
            imageUrl = albumArt,
            placeholderIcon = placeholderIcon
        )

        Spacer(modifier = Modifier.width(DisplayItemDefaults.InternalSpacing))

        ItemInfoColumn(
            modifier = Modifier.weight(1f),
            title = title,
            subtitle = subtitle,
            titleSize = titleSize,
            subtitleSize = subtitleSize,
            spacing = spacing
        )

        MoreMenuButton(content = menu)
    }
}

@Composable
private fun GridItem(
    modifier: Modifier,
    title: String,
    subtitle: String,
    albumArt: String?,
    placeholderIcon: ImageVector
) {
    Column(
        modifier = modifier.padding(DisplayItemDefaults.GridPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ItemImage(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth(),
            imageUrl = albumArt,
            placeholderIcon = placeholderIcon
        )
        Spacer(modifier = Modifier.height(DisplayItemDefaults.GridTextTopMargin))
        ItemInfoColumn(
            modifier = Modifier.fillMaxWidth(),
            title = title,
            subtitle = subtitle,
            titleSize = DisplayItemDefaults.TitleSizeNormal,
            subtitleSize = DisplayItemDefaults.SubtitleSizeNormal,
            spacing = DisplayItemDefaults.SpacingHigh,
            horizontalAlignment = Alignment.CenterHorizontally
        )
    }
}

@Composable
private fun ItemImage(
    modifier: Modifier,
    imageUrl: String?,
    placeholderIcon: ImageVector
) {
    ArtworkImage(
        modifier = modifier
            .clip(RoundedCornerShape(DisplayItemDefaults.ImageCorner))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        imageUrl = imageUrl,
        placeholderIcon = placeholderIcon
    )
}

@Composable
private fun ItemInfoColumn(
    modifier: Modifier,
    title: String,
    subtitle: String,
    titleSize: TextUnit,
    subtitleSize: TextUnit,
    spacing: Dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Text(
            text = title,
            fontSize = titleSize,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(spacing))
        Text(
            text = subtitle,
            fontSize = subtitleSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MoreMenuButton(
    content: @Composable (OptionMenuScope.() -> Unit)? = null
) {
    if (content == null) return

    var showMore by remember { mutableStateOf(false) }

    val onDismiss = remember { { showMore = false } }
    val onClick = remember { { showMore = true } }

    Box {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = Icons.Rounded.MoreVert.name
            )
        }
        OptionMenu(
            expanded = showMore,
            onDismissRequest = onDismiss,
            content = content
        )
    }
}
