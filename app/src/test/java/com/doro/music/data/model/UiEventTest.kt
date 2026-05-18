package com.doro.music.data.model

import org.junit.Assert.*
import org.junit.Test

class UiEventTest {

    @Test
    fun `SongAddedToPlaylist with Success has correct messageResId`() {
        val event = UiEvent.SongAddedToPlaylist(AddSongResult.Success)
        // Just verify it's a valid resource ID (non-zero)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `SongAddedToPlaylist with AlreadyExists has correct messageResId`() {
        val event = UiEvent.SongAddedToPlaylist(AddSongResult.AlreadyExists)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `SongAddedToPlaylist with Failed has correct messageResId`() {
        val event = UiEvent.SongAddedToPlaylist(AddSongResult.Failed)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `SongAddedToPlaylist different results have different messageResIds`() {
        val success = UiEvent.SongAddedToPlaylist(AddSongResult.Success)
        val alreadyExists = UiEvent.SongAddedToPlaylist(AddSongResult.AlreadyExists)
        val failed = UiEvent.SongAddedToPlaylist(AddSongResult.Failed)

        assertNotEquals(success.messageResId, alreadyExists.messageResId)
        assertNotEquals(success.messageResId, failed.messageResId)
        assertNotEquals(alreadyExists.messageResId, failed.messageResId)
    }

    @Test
    fun `SongRemovedFromPlaylist success has correct messageResId`() {
        val event = UiEvent.SongRemovedFromPlaylist(true)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `SongRemovedFromPlaylist failure has correct messageResId`() {
        val event = UiEvent.SongRemovedFromPlaylist(false)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `SongRemovedFromPlaylist success and failure have different messageResIds`() {
        val success = UiEvent.SongRemovedFromPlaylist(true)
        val failure = UiEvent.SongRemovedFromPlaylist(false)
        assertNotEquals(success.messageResId, failure.messageResId)
    }

    @Test
    fun `PlaylistCreated success has correct messageResId`() {
        val event = UiEvent.PlaylistCreated(true)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `PlaylistCreated failure has correct messageResId`() {
        val event = UiEvent.PlaylistCreated(false)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `PlaylistDeleted success has correct messageResId`() {
        val event = UiEvent.PlaylistDeleted(true)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `PlaylistDeleted failure has correct messageResId`() {
        val event = UiEvent.PlaylistDeleted(false)
        assertNotEquals(0, event.messageResId)
    }

    @Test
    fun `UiEvent implementations are distinct types`() {
        val added = UiEvent.SongAddedToPlaylist(AddSongResult.Success)
        val removed = UiEvent.SongRemovedFromPlaylist(true)
        val created = UiEvent.PlaylistCreated(true)
        val deleted = UiEvent.PlaylistDeleted(true)

        assertNotEquals(added, removed)
        assertNotEquals(added, created)
        assertNotEquals(added, deleted)
        assertNotEquals(removed, created)
        assertNotEquals(removed, deleted)
        assertNotEquals(created, deleted)
    }
}