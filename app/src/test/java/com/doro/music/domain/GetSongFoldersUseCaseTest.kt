package com.doro.music.domain

import com.doro.music.data.model.Folder
import com.doro.music.data.repo.FolderRepo
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetSongFoldersUseCaseTest {

    private val mockRepo = mockk<FolderRepo>()
    private val useCase = GetSongFoldersUseCase(mockRepo)

    @Test
    fun `invoke returns Flow from repo folders`() = runTest {
        val folders = listOf(
            Folder(path = "/music/rock", songCount = 10),
            Folder(path = "/music/pop", songCount = 20)
        )
        io.mockk.every { mockRepo.folders } returns flowOf(folders)

        val result = useCase()

        result.collect { collectedFolders ->
            assertEquals(2, collectedFolders.size)
            assertEquals("/music/rock", collectedFolders[0].path)
            assertEquals("/music/pop", collectedFolders[1].path)
        }
    }

    @Test
    fun `invoke returns empty Flow when no folders`() = runTest {
        io.mockk.every { mockRepo.folders } returns flowOf(emptyList())

        val result = useCase()

        result.collect { collectedFolders ->
            assertTrue(collectedFolders.isEmpty())
        }
    }
}
