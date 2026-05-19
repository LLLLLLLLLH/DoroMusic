package com.doro.music.player.lyrics

import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.LyricsLine
import com.doro.music.data.model.LyricsSource

object LrcParser {

    private val TIMESTAMP_PATTERN = Regex("""\[(\d{2,3}):(\d{2})\.(\d{2,3})]""")
    private val OFFSET_PATTERN = Regex("""\[offset:([+-]?\d+)]""")

    fun parse(lrcText: String): LyricsData? {
        val lines = mutableListOf<LyricsLine>()
        var offset = 0L

        lrcText.lines().forEach { line ->
            OFFSET_PATTERN.find(line)?.let {
                offset = it.groupValues[1].toLong()
                return@forEach
            }

            val timestamps = TIMESTAMP_PATTERN.findAll(line).toList()
            if (timestamps.isNotEmpty()) {
                // 歌词文本 = 移除所有时间戳标签后的内容
                val text = line.replace(TIMESTAMP_PATTERN, "").trim()
                if (text.isEmpty()) return@forEach

                timestamps.forEach { match ->
                    val min = match.groupValues[1].toLong()
                    val sec = match.groupValues[2].toLong()
                    val ms = match.groupValues[3].let { raw ->
                        when (raw.length) {
                            2 -> raw.toLong() * 10
                            3 -> raw.toLong()
                            else -> raw.toLong()
                        }
                    }
                    val timeMs = (min * 60 + sec) * 1000 + ms
                    lines.add(LyricsLine(timeMs = timeMs, text = text))
                }
            }
        }

        if (lines.isEmpty()) return null
        return LyricsData(
            songId = 0,
            source = LyricsSource.LOCAL_LRC,
            lines = lines.sortedBy { it.timeMs },
            offset = offset
        )
    }
}
