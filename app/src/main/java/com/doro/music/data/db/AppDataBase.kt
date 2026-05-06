package com.doro.music.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doro.music.data.db.dao.ArtistDao
import com.doro.music.data.db.dao.FolderDao
import com.doro.music.data.db.dao.PlayQueueDao
import com.doro.music.data.db.dao.PlaylistDao
import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.db.dao.SearchDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.entities.FolderEntity
import com.doro.music.data.db.entities.PlayQueueEntity
import com.doro.music.data.db.entities.PlaylistEntity
import com.doro.music.data.db.entities.PlaylistSongEntity
import com.doro.music.data.db.entities.SongEntity

@Database(
    entities = [SongEntity::class, PlaylistEntity::class, PlaylistSongEntity::class, PlayQueueEntity::class, FolderEntity::class],
    version = 9,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun artistDao(): ArtistDao
    abstract fun playQueueDao(): PlayQueueDao
    abstract fun searchDao(): SearchDao
    abstract fun folderDao(): FolderDao

    companion object {
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }
    }
}
