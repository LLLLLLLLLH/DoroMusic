@file:OptIn(ExperimentalMaterial3Api::class)

package com.doro.music.ui.screen.other

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.doro.music.R
import com.doro.music.data.model.Song
import com.doro.music.ext.formatDuration
import com.doro.music.ext.safePop
import com.doro.music.ui.component.IconAction
import com.doro.music.ui.component.placeholderPainter
import kotlinx.serialization.Serializable

@Serializable
data class SongDetail(val song: Song) : NavKey

fun EntryProviderScope<NavKey>.songDetailRoute(backStack: NavBackStack<NavKey>) {
    entry<SongDetail> {
        SongDetailScreen(
            song = it.song,
            onBack = backStack::safePop
        )
    }
}

@Composable
private fun SongDetailScreen(song: Song, onBack: () -> Unit) {

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.details)) },
                navigationIcon = {
                    IconAction(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        onClick = onBack
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.album_cover),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                AsyncImage(
                    modifier = Modifier.size(120.dp),
                    model = song.albumArt,
                    error = placeholderPainter(),
                    fallback = placeholderPainter(),
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(R.string.album_cover)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            DetailRow(labelResId = R.string.title, value = song.title)
            DetailRow(labelResId = R.string.artists, value = song.artist, fallbackResId = R.string.unknown_artist)
            DetailRow(labelResId = R.string.album, value = song.album, fallbackResId = R.string.unknown_album)
            DetailRow(labelResId = R.string.genre, value = song.genre, fallbackResId = R.string.unknown_genre)
            DetailRow(labelResId = R.string.year, value = song.year?.toString(), fallbackResId = R.string.unknown_year)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            DetailRow(labelResId = R.string.duration, value = song.duration.formatDuration())
            DetailRow(labelResId = R.string.mime_type, value = song.mimeType, fallbackResId = R.string.unknown_mime_type)
            DetailRow(labelResId = R.string.path, value = song.path)
        }
    }
}

@Composable
private fun DetailRow(labelResId: Int, value: String?, fallbackResId: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(labelResId),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = value ?: fallbackResId?.let { stringResource(it) } ?: "",
            maxLines = 3,
            fontSize = 12.sp,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
