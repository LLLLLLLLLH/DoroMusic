package com.doro.music.data.db.entities

import com.doro.music.data.model.Artist

data class ArtistEntity(
    val name: String,
    val songCount: Int
)

fun ArtistEntity.toArtist() = Artist(name = name, songCount = songCount)

fun List<ArtistEntity>.toArtists() = map { it.toArtist() }
