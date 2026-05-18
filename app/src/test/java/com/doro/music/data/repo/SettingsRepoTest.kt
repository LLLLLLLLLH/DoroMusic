package com.doro.music.data.repo

import com.doro.music.data.db.dao.FolderDao
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SettingsRepoTest {

    private val mockFolderDao = mockk<FolderDao>()
    private lateinit var repo: SettingsRepo

    @Before
    fun setup() {
        repo = SettingsRepo(mockFolderDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `setExcludedFolders delegates to DAO`() = runTest {
        val folders = listOf("/music/excluded1", "/music/excluded2")
        coEvery { mockFolderDao.setExcludedFolders(folders) } just Runs

        repo.setExcludedFolders(folders)

        coVerify { mockFolderDao.setExcludedFolders(folders) }
    }

    @Test
    fun `setExcludedFolders with empty list delegates to DAO`() = runTest {
        coEvery { mockFolderDao.setExcludedFolders(emptyList()) } just Runs

        repo.setExcludedFolders(emptyList())

        coVerify { mockFolderDao.setExcludedFolders(emptyList()) }
    }
}