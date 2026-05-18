package com.doro.music.player.model

sealed class PlayAction {

    data object Prev : PlayAction()
    data object Next : PlayAction()
    data class Play(val songId: Long, val playContext: PlayContext) : PlayAction()
    data object TogglePlay : PlayAction()
    data object TogglePlayMode : PlayAction()
    data class SeekTo(val positionMs: Long) : PlayAction()
    data class SeekToQueueItem(val queueId: Long) : PlayAction()
    data class InsertSingle(val songId: Long) : PlayAction()
    data class InsertGroup(val playContext: PlayContext) : PlayAction()
    data class Remove(val queueId: Long) : PlayAction()
}