package com.doro.music.player.model

import com.doro.music.data.model.SortMode

sealed interface PlayContext {
    val sortMode: SortMode

    data class All(override val sortMode: SortMode) : PlayContext
    data class Artist(val artist: String, override val sortMode: SortMode) : PlayContext
    data class Folder(val path: String, override val sortMode: SortMode) : PlayContext
    data class Playlist(val playlistId: Long, override val sortMode: SortMode) : PlayContext
    data class Search(val keyword: String, override val sortMode: SortMode) : PlayContext
}
