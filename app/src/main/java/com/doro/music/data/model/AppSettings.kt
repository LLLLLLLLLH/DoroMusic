package com.doro.music.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val defaultPlayMode: PlayMode = PlayMode.REPEAT,
    val darkTheme: DarkThemeMode = DarkThemeMode.SYSTEM,
    val minDurationFilter: Int = 30
)
