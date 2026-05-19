package com.doro.music.player.lyrics

import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.LyricsSource
import com.doro.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 本地 .lrc 文件加载器
 *
 * 根据音频文件路径查找同名 .lrc 文件并解析
 */
class LrcFileLoader {

    /**
     * 根据音频文件路径查找同名 .lrc 文件
     * 例: /storage/Music/song.mp3 -> /storage/Music/song.lrc
     */
    suspend fun loadForSong(song: Song): LyricsData? = withContext(Dispatchers.IO) {
        val audioPath = song.path
        val lrcPath = audioPath.replaceAfterLast('.', "lrc")
        val lrcFile = File(lrcPath)

        if (lrcFile.exists()) {
            return@withContext parseLrcFile(lrcFile, song)
        }

        // 尝试大小写不敏感匹配
        findAlternativeLrc(audioPath)?.let { return@withContext parseLrcFile(it, song) }
        null
    }

    private fun parseLrcFile(file: File, song: Song): LyricsData? {
        val content = file.readText(Charsets.UTF_8)
        return LrcParser.parse(content)?.copy(
            songId = song.id,
            source = LyricsSource.LOCAL_LRC
        )
    }

    private fun findAlternativeLrc(audioPath: String): File? {
        val dir = File(audioPath).parentFile ?: return null
        val baseName = File(audioPath).nameWithoutExtension
        return dir.listFiles()?.firstOrNull {
            it.extension.equals("lrc", ignoreCase = true) &&
                    it.nameWithoutExtension.equals(baseName, ignoreCase = true)
        }
    }
}
