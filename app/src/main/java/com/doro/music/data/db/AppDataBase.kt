package com.doro.music.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doro.music.data.db.dao.ArtistDao
import com.doro.music.data.db.dao.FolderDao
import com.doro.music.data.db.dao.PlaylistDao
import com.doro.music.data.db.dao.PlaylistSongDao
import com.doro.music.data.db.dao.SearchDao
import com.doro.music.data.db.dao.SongDao
import com.doro.music.data.db.dao.PlayQueueDao
import com.doro.music.data.db.entities.PlayerQueueEntity
import com.doro.music.data.db.entities.FolderEntity
import com.doro.music.data.db.entities.PlaylistEntity
import com.doro.music.data.db.entities.PlaylistSongEntity
import com.doro.music.data.db.entities.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        PlayerQueueEntity::class,
        FolderEntity::class
    ],
    version = 10,
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

        /**
         * 9 → 10：重建 play_queue 表
         *
         * 旧表：id (PK, auto), songId
         * 新表：queueId (PK, auto), song_id, sort_order, shuffle_order
         *
         * 由于表结构完全不同，采用删除旧表 + 创建新表的方式
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS play_queue")
                db.execSQL(
                    """
                    CREATE TABLE play_queue (
                        queue_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        song_id INTEGER NOT NULL,
                        sort_order TEXT NOT NULL,
                        shuffle_order TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX index_play_queue_sort_order ON play_queue(sort_order)")
                db.execSQL("CREATE INDEX index_play_queue_shuffle_order ON play_queue(shuffle_order)")
                db.execSQL("CREATE INDEX index_play_queue_song_id ON play_queue(song_id)")
            }
        }
    }
}