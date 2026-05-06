package com.doro.music.data.model

sealed class PlaybackEvent {
    data class OnPlaybackStateChanged(val state: PlaybackState) : PlaybackEvent()
    data class OnMediaItemTransition(val song: Song?, val reason: Int) : PlaybackEvent()
    data class OnPositionChanged(val positionMs: Long) : PlaybackEvent()
    data class OnDurationChanged(val durationMs: Long) : PlaybackEvent()
    data class OnPlayModeChanged(val mode: PlayMode) : PlaybackEvent()
    data class OnPlaylistRestored(val songs: List<Song>, val currentIndex: Int, val currentPosition: Long) : PlaybackEvent()
    data class OnError(val exception: Exception, val errorCode: Int = -1) : PlaybackEvent()
}
