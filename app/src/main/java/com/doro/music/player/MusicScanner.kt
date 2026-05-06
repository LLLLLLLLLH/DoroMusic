package com.doro.music.player

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.doro.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed class ScanResult {
    data class Success(val songs: List<Song>) : ScanResult()
    data class Error(val throwable: Throwable?) : ScanResult()
}

class MusicScanner(private val context: Context) {

    private val resolver by lazy { context.contentResolver }

    suspend fun scan(
        minDurationSeconds: Int = 0,
        excludedFolders: List<String> = emptyList()
    ): ScanResult = withContext(Dispatchers.IO) {
        try {
            val selection = buildSelection(minDurationSeconds)
            val selectionArgs = if (minDurationSeconds > 0)
                arrayOf(minDurationSeconds.toLong().toString())
            else null
            val cursor = resolver.query(
                EXTERNAL_URI, PROJECTION, selection, selectionArgs, SORT_ORDER
            ) ?: return@withContext ScanResult.Error(null)
            val songs = parse(cursor)
                .filterNot { song -> isExcluded(song.path, excludedFolders) }
            ScanResult.Success(songs)
        } catch (e: Exception) {
            return@withContext ScanResult.Error(e)
        }
    }

    private fun buildSelection(minDurationSeconds: Int): String {
        if (minDurationSeconds <= 0) return BASE_SELECTION
        return "$BASE_SELECTION AND ${MediaStore.Audio.Media.DURATION} >= ?"
    }

    private fun isExcluded(songPath: String, excludedFolders: List<String>): Boolean {
        return excludedFolders.any { folder ->
            songPath == folder || songPath.startsWith(folder + File.separator)
        }
    }

    private fun parse(cursor: Cursor): List<Song> = buildList {
        cursor.use { c ->
            val cols = ColumnIndex(c)
            while (c.moveToNext()) {
                runCatching { add(cols.toSong(c)) }
            }
        }
    }

    private class ColumnIndex(cursor: Cursor) {
        val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val dateAdded = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
        val size = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
        val year = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR).takeIf { it >= 0 }
        val mimeType = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE).takeIf { it >= 0 }
        val genre = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            cursor.getColumnIndex(MediaStore.Audio.Media.GENRE).takeIf { it >= 0 }
        else null

        fun toSong(c: Cursor) = Song(
            id = c.getLong(id),
            uri = ContentUris.withAppendedId(EXTERNAL_URI, c.getLong(id)).toString(),
            title = c.getString(title) ?: "",
            artist = c.getString(artist).takeIf { it?.isNotBlank() == true },
            album = c.getString(album).takeIf { it?.isNotBlank() == true },
            albumArt = ALBUM_ART_URI_TEMPLATE.format(c.getLong(albumId)),
            year = year?.let { c.getInt(it) }?.takeIf { it > 0 },
            duration = c.getLong(duration),
            mimeType = mimeType?.let { c.getString(it) },
            path = c.getString(data) ?: "",
            genre = genre?.let { c.getString(it) },
            dateAdded = c.getLong(dateAdded),
            size = c.getLong(size)
        )
    }

    companion object {
        private const val SORT_ORDER = MediaStore.Audio.Media.TITLE + " ASC"
        private val EXTERNAL_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        private const val ALBUM_ART_URI_TEMPLATE = "content://media/external/audio/albumart/%d"

        private const val BASE_SELECTION = """
            ${MediaStore.Audio.Media.IS_MUSIC} != 0
            AND ${MediaStore.Audio.Media.SIZE} > 0
        """

        private val PROJECTION = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATA)
            add(MediaStore.Audio.Media.DATE_ADDED)
            add(MediaStore.Audio.Media.SIZE)
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.MIME_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.Audio.Media.GENRE)
            }
        }.toTypedArray()

        private val FOLDER_PROJECTION = arrayOf(MediaStore.Audio.Media.DATA)
    }
}
