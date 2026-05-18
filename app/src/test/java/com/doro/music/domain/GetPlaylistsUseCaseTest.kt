@file:Suppress("UNUSED_EXPRESSION")

package com.doro.music.domain

import androidx.paging.PagingData
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.SortMode
import com.doro.music.data.repo.PlaylistRepo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPlaylistsUseCaseTest {

    private val mockRepo = mockk<PlaylistRepo>()
    private lateinit var useCase: GetPlaylistsUseCase

    @Before
    fun setup() {
        every { mockRepo.getPlaylists(any()) } returns flowOf(PagingData.empty())
        useCase = GetPlaylistsUseCase(mockRepo)
    }

    @Test
    fun `invoke returns non-null Flow`() = runTest {
        val result = useCase().first()
        assertNotNull(result)
    }

    @Test
    fun `invoke delegates to repo with DATE_ADDED sort mode`() = runTest {
        val sortModeSlot = slot<SortMode>()
        every { mockRepo.getPlaylists(capture(sortModeSlot)) } returns flowOf(PagingData.empty())

        useCase().first()

        assertEquals(SortMode.DATE_ADDED, sortModeSlot.captured)
    }

    @Test
    fun `invoke returns the flow from repo`() = runTest {
        val playlist = Playlist(id = 1L, name = "Test", songCount = 5)
        val pagingData = PagingData.from(listOf(playlist))
        every { mockRepo.getPlaylists(SortMode.DATE_ADDED) } returns flowOf(pagingData)

        val result = useCase().first()

        assertNotNull(result)
    }

    @Test
    fun `invoke always uses DATE_ADDED regardless of call count`() = runTest {
        val callCount = mutableListOf<SortMode>()
        every { mockRepo.getPlaylists(capture(callCount)) } returns flowOf(PagingData.empty())

        useCase().first()
        useCase().first()
        useCase().first()

        assertEquals(3, callCount.size)
        assertTrue(callCount.all { it == SortMode.DATE_ADDED })
    }
}
