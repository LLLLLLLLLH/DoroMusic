package com.doro.music.ui.component

import androidx.annotation.StringRes
import com.doro.music.R

enum class MainTab(@StringRes val titleRes: Int) {
    Songs(R.string.songs),
    Artists(R.string.artists),
    Folders(R.string.folders),
    Playlists(R.string.playlists)
}
