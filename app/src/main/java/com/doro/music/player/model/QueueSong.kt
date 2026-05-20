package com.doro.music.player.model

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Embedded
import com.doro.music.data.db.entities.SongEntity

data class QueueSong(
    @Embedded
    val song: SongEntity,
    val queueId: Long,
    val sortOrder: String,
    val shuffleOrder: String
)

fun QueueSong.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(queueId.toString())
        .setUri(song.uri?.toUri())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.albumArt?.toUri())
                .build()
        )
        .build()
}
