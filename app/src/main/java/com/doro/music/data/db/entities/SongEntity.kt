package com.doro.music.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.doro.music.data.model.Song

@Entity(
    tableName = "songs",
    indices = [Index(value = ["path"], unique = true)]
)
data class SongEntity(
    @PrimaryKey
    val id: Long,
    val uri: String? = null,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val albumArt: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val duration: Long = 0,
    val mimeType: String? = null,
    val path: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val size: Long = 0
)

fun SongEntity.toSong() = Song(
    id = id,
    uri = uri,
    title = title,
    artist = artist,
    album = album,
    albumArt = albumArt,
    year = year,
    duration = duration,
    mimeType = mimeType,
    genre = genre,
    path = path,
    dateAdded = dateAdded,
    size = size
)

fun Song.toEntity() = SongEntity(
    id = id,
    uri = uri,
    title = title,
    artist = artist,
    album = album,
    albumArt = albumArt,
    year = year,
    duration = duration,
    mimeType = mimeType,
    genre = genre,
    path = path,
    dateAdded = dateAdded,
    size = size
)

fun List<SongEntity>.toSongs() = map { it.toSong() }

fun List<Song>.toSongEntities() = map { it.toEntity() }
