package com.doro.music.data.repo

import com.doro.music.data.db.dao.FolderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepo(
    private val folderDao: FolderDao
) {

    suspend fun setExcludedFolders(folders: List<String>) = withContext(Dispatchers.IO) {
        folderDao.setExcludedFolders(folders)
    }

}