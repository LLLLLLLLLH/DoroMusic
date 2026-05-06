package com.doro.music.player

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.doro.music.data.model.Song
import java.io.File

object MediaExtras {
    const val SONG_ID = "song_id"
    const val SONG_URI = "song_uri"
    const val SONG_ALBUM_ART = "song_album_art"
    const val SONG_DURATION = "song_duration"
    const val SONG_PATH = "song_path"
}

fun Song.toMediaItem(): MediaItem {
    val uri = this.uri?.toUri() ?: Uri.fromFile(File(path))
    val extras = Bundle().apply {
        putLong(MediaExtras.SONG_ID, id)
        putString(MediaExtras.SONG_URI, this@toMediaItem.uri)
        putString(MediaExtras.SONG_ALBUM_ART, albumArt)
        putLong(MediaExtras.SONG_DURATION, duration)
        putString(MediaExtras.SONG_PATH, path)
    }
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setExtras(extras)
                .build()
        )
        .build()
}
