package com.doro.music.data.model

import com.doro.music.R

enum class SortMode(val labelResId: Int) {
    TITLE(R.string.sort_by_name),
    ARTIST(R.string.sort_by_artist),
    DATE_ADDED(R.string.sort_by_date_added)
}
