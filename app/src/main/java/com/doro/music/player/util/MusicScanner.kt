package com.doro.music.player.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import co.touchlab.kermit.Logger
import com.doro.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ScanResult {
    data class Success(val songs: List<Song>) : ScanResult()
    data class Error(val throwable: Throwable?) : ScanResult()
}

class MusicScanner(private val context: Context) {

    private val log = Logger.withTag("MusicScanner")
    private val resolver by lazy { context.contentResolver }

    suspend fun scan(
        minDurationSeconds: Int = 0,
        excludedFolders: List<String> = emptyList()
    ): ScanResult = try {
        val songs = queryMediaStore(minDurationSeconds, excludedFolders)
        ScanResult.Success(songs)
    } catch (e: Exception) {
        log.e("Scan failed", e)
        ScanResult.Error(e)
    }

    private suspend fun queryMediaStore(
        minDurationSeconds: Int = 0,
        excludedFolders: List<String> = emptyList()
    ): List<Song> = withContext(Dispatchers.IO) {
        val args = minDurationSeconds.takeIf { it > 0 }
            ?.let { arrayOf((it * MS_PER_SECOND).toString()) }

        val cursor = resolver.query(
            EXTERNAL_URI, PROJECTION, selection(minDurationSeconds), args, SORT_ORDER
        ) ?: return@withContext emptyList()

        cursor.toSongList().filterNot { it.isExcluded(excludedFolders) }
    }

    private fun selection(minDuration: Int): String {
        val base = "(${MediaStore.Audio.Media.IS_MUSIC} = 1 OR ${MediaStore.Audio.Media.IS_MUSIC} IS NULL) AND ${MediaStore.Audio.Media.SIZE} > 0"
        return if (minDuration > 0) "$base AND ${MediaStore.Audio.Media.DURATION} >= ?" else base
    }

    private fun Song.isExcluded(folders: List<String>): Boolean =
        folders.any { path == it || path.startsWith("$it/") }

    // ==================== Cursor → Song ====================

    private fun Cursor.toSongList(): List<Song> = buildList {
        use { c ->
            val idx = ColumnIndex(c)
            while (c.moveToNext()) {
                try {
                    add(idx.readSong(c))
                } catch (e: Exception) {
                    log.w("Skip row ${c.position}", e)
                }
            }
        }
    }

    private class ColumnIndex(c: Cursor) {
        val id = c.column(MediaStore.Audio.Media._ID)
        val title = c.column(MediaStore.Audio.Media.TITLE)
        val artist = c.column(MediaStore.Audio.Media.ARTIST)
        val album = c.column(MediaStore.Audio.Media.ALBUM)
        val albumId = c.column(MediaStore.Audio.Media.ALBUM_ID)
        val duration = c.column(MediaStore.Audio.Media.DURATION)
        val data = c.column(MediaStore.Audio.Media.DATA)
        val dateAdded = c.column(MediaStore.Audio.Media.DATE_ADDED)
        val size = c.column(MediaStore.Audio.Media.SIZE)
        val year = c.columnOptional(MediaStore.Audio.Media.YEAR)
        val mimeType = c.columnOptional(MediaStore.Audio.Media.MIME_TYPE)
        val genre = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            c.columnOptional(MediaStore.Audio.Media.GENRE) else null

        fun readSong(c: Cursor) = Song(
            id = c.getLong(id),
            uri = ContentUris.withAppendedId(EXTERNAL_URI, c.getLong(id)).toString(),
            title = c.getString(title).orEmpty(),
            artist = c.getString(artist).orBlank(),
            album = c.getString(album).orBlank(),
            albumArt = ALBUM_ART_URI.format(c.getLong(albumId)),
            year = year?.let { c.getInt(it) }?.takeIf { it > 0 },
            duration = c.getLong(duration),
            mimeType = mimeType?.let { c.getString(it) },
            path = c.getString(data).orEmpty(),
            genre = genre?.let { c.getString(it) },
            dateAdded = c.getLong(dateAdded),
            size = c.getLong(size)
        )
    }

    companion object {
        private const val MS_PER_SECOND = 1000L
        private const val SORT_ORDER = "${MediaStore.Audio.Media.TITLE} ASC"
        private const val ALBUM_ART_URI = "content://media/external/audio/albumart/%d"
        private val EXTERNAL_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        private val PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            *if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) arrayOf(MediaStore.Audio.Media.GENRE) else emptyArray()
        )
    }
}

private fun Cursor.column(name: String) = getColumnIndexOrThrow(name)
private fun Cursor.columnOptional(name: String) = getColumnIndex(name).takeIf { it >= 0 }
private fun String?.orBlank() = takeUnless { it.isNullOrBlank() }
