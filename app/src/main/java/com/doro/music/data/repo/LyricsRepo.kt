package com.doro.music.data.repo

import com.doro.music.data.db.dao.LyricsDao
import com.doro.music.data.db.entities.LyricsEntity
import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.LyricsSource
import com.doro.music.data.model.Song
import com.doro.music.player.lyrics.LrcFileLoader
import com.doro.music.player.lyrics.LrcParser

class LyricsRepo(
    private val lrcFileLoader: LrcFileLoader,
    private val lyricsDao: LyricsDao
) {

    /**
     * 获取歌词，按优先级链尝试：
     * 1. Room 缓存
     * 2. 本地 .lrc 文件
     * 3. 音频内嵌歌词（ID3）
     */
    suspend fun getLyrics(song: Song): LyricsData? {
        // 1. 检查缓存
        lyricsDao.getBySongId(song.id)?.let { entity ->
            return LrcParser.parse(entity.lrcContent)?.copy(
                songId = song.id,
                source = LyricsSource.CACHE,
                offset = entity.offset
            )
        }

        // 2. 本地 LRC 文件
        lrcFileLoader.loadForSong(song)?.let { lyrics ->
            cacheLyrics(song.id, lyrics)
            return lyrics
        }

        return null
    }

    private suspend fun cacheLyrics(songId: Long, lyrics: LyricsData) {
        val lrcContent = rebuildLrc(lyrics)
        lyricsDao.insert(
            LyricsEntity(
                songId = songId,
                lrcContent = lrcContent,
                source = lyrics.source.name
            )
        )
    }

    private fun rebuildLrc(lyrics: LyricsData): String {
        return lyrics.lines
            .filter { it.timeMs >= 0 }
            .joinToString("\n") { line ->
                val min = line.timeMs / 60000
                val sec = (line.timeMs % 60000) / 1000
                val ms = (line.timeMs % 1000) / 10
                "[%02d:%02d.%02d]%s".format(min, sec, ms, line.text)
            }
    }
}
