package com.doro.music.data.model

import com.doro.music.R

enum class DisplayMode(val labelResId: Int)  {
    COMPACT(R.string.display_compact),
    LIST(R.string.display_list),
    GRID(R.string.display_grid)
}
