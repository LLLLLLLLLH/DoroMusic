package com.doro.music.data.repo

import com.doro.music.data.datastore.SettingsDataStore
import com.doro.music.data.db.dao.FolderDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.toFolderEntities
import com.doro.music.data.db.entities.toSongEntities
import com.doro.music.ext.orDefault
import com.doro.music.player.util.MusicScanner
import com.doro.music.player.util.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * 主仓库（移除旧架构依赖）
 * 
 * 注意：扫描功能需要重新实现或移除
 */
class MainRepo(
    private val songDao: SongDao,
    private val folderDao: FolderDao,
    private val settingsDataStore: SettingsDataStore,
    private val musicScanner: MusicScanner,
) {
    suspend fun scan(): ScanResult = withContext(Dispatchers.IO) {

        val minDurationSeconds = settingsDataStore.settings.firstOrNull()?.minDurationFilter.orDefault(0)
        val excludedFolders = folderDao.getAllFolders().firstOrNull()?.filter { it.excluded }?.map { it.path }.orEmpty()

        val scanResult = musicScanner.scan(minDurationSeconds, excludedFolders)

        when (scanResult) {
            is ScanResult.Error -> Unit
            is ScanResult.Success -> {
                songDao.syncSongs(scanResult.songs.toSongEntities())
                folderDao.syncFolders(scanResult.songs.toFolderEntities())
            }
        }

        return@withContext scanResult
    }
}
