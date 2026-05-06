package com.doro.music.data.model

sealed interface AddSongResult {
    data object Success : AddSongResult
    data object AlreadyExists : AddSongResult
    data object Failed : AddSongResult
}
