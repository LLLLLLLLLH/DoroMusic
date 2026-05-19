package com.doro.music.data.model

import androidx.compose.runtime.Immutable

/**
 * 歌词来源类型
 */
enum class LyricsSource {
    LOCAL_LRC,
    CACHE
}

/**
 * 单行歌词
 *
 * @param timeMs 该行开始时间（毫秒），-1 表示无时间同步（纯文本歌词）
 * @param text 歌词文本
 */
@Immutable
data class LyricsLine(
    val timeMs: Long,
    val text: String
)

/**
 * 完整歌词数据
 *
 * @param songId 关联的歌曲 ID
 * @param source 歌词来源
 * @param lines 按时间排序的歌词行列表
 * @param offset 用户调整的时间偏移量（毫秒）
 */
@Immutable
data class LyricsData(
    val songId: Long,
    val source: LyricsSource,
    val lines: List<LyricsLine>,
    val offset: Long = 0
)
