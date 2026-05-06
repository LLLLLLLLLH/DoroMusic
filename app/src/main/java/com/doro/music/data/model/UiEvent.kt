package com.doro.music.data.model

import com.doro.music.R

sealed interface UiEvent {
    val messageResId: Int

    data class SongAddedToPlaylist(val result: AddSongResult) : UiEvent {
        override val messageResId: Int = when (result) {
            AddSongResult.AlreadyExists -> R.string.song_already_in_playlist
            AddSongResult.Failed -> R.string.add_failed
            AddSongResult.Success -> R.string.add_success
        }
    }

    data class SongRemovedFromPlaylist(val success: Boolean) : UiEvent {
        override val messageResId: Int = if (success) R.string.remove_success else R.string.remove_failed
    }

    data class PlaylistCreated(val success: Boolean) : UiEvent {
        override val messageResId: Int = if (success) R.string.playlist_created_success else R.string.playlist_created_failed
    }

    data class PlaylistDeleted(val success: Boolean) : UiEvent {
        override val messageResId: Int = if (success) R.string.playlist_deleted_success else R.string.playlist_deleted_failed
    }
}
