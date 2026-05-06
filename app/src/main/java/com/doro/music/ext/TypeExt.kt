package com.doro.music.ext

import androidx.compose.ui.text.intl.Locale

fun Long.formatDuration(): String {
    if (this <= 0) return "00:00"
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.current.platformLocale,"%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.current.platformLocale,"%02d:%02d", minutes, seconds)
    }
}