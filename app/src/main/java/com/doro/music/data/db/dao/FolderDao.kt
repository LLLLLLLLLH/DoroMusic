package com.doro.music.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.doro.music.data.db.entities.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY path ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT path FROM folders")
    suspend fun getAllPaths(): List<String>

    @Query("SELECT path FROM folders WHERE excluded = 1")
    suspend fun getExcludedPaths(): List<String>

    @Query("UPDATE folders SET excluded = :excluded WHERE path = :path")
    suspend fun setExcluded(path: String, excluded: Boolean)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun syncFolders(folders: List<FolderEntity>)

    @Transaction
    suspend fun setExcludedFolders(paths: List<String>){
        val excludedPaths = getExcludedPaths().toSet()
        val newExcluded = paths.toSet()

        val toInclude = excludedPaths - newExcluded
        val toExclude = newExcluded - excludedPaths

        toInclude.forEach { setExcluded(it, false) }
        toExclude.forEach { setExcluded(it, true) }
    }

}