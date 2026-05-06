package com.doro.music.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doro.music.data.model.Folder
import com.doro.music.data.model.Song
import java.io.File

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val path: String,
    val songCount: Int,
    val excluded: Boolean = false
)

fun FolderEntity.toFolder() = Folder(
    path = path,
    songCount = songCount,
    excluded = excluded
)

fun List<FolderEntity>.toFolders() = map { it.toFolder() }

fun List<Song>.toFolderEntities() = groupBy { File(it.path).parent }
    .mapNotNull { (path, songs) -> path?.let { FolderEntity(path = it, songCount = songs.size) } }
