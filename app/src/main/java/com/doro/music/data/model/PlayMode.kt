package com.doro.music.data.model

import com.doro.music.ext.orDefault

enum class PlayMode {
    REPEAT,
    SHUFFLE,
    REPEAT_ONE;

    companion object {
        fun pares(name: String?) = runCatching { name?.let { PlayMode.valueOf(it) } }
            .getOrDefault(REPEAT)
            .orDefault(REPEAT)
    }
}
