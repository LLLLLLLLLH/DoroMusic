package com.doro.music.data.model

/**
 * 播放快照 - 播放系统的内部状态
 * 这是Repository维护的真实状态，完整表示了当前播放的所有信息
 * 与ExoPlayer的状态一一对应
 *
 * @property songs 当前播放队列
 * @property currentIndex 当前播放的歌曲索引
 * @property currentPosition 当前播放位置（毫秒）
 * @property duration 当前歌曲总时长（毫秒）
 * @property isPlaying 是否正在播放
 * @property playMode 播放模式（顺序/随机/单曲循环）
 * @property isLoading 是否正在加载中
 * @property errorMessage 错误信息（为null表示无错误）
 *
 * @author DoroMusic
 */
data class PlaybackSnapshot(
    val songs: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isPlaying: Boolean = false,
    val playMode: PlayMode = PlayMode.REPEAT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** 空状态快照 */
        val EMPTY = PlaybackSnapshot()

    }

    /**
     * 获取当前播放的歌曲
     * @return 当前歌曲，如果索引无效则返回null
     */
    fun getCurrentSong(): Song? = songs.getOrNull(currentIndex)

    /**
     * 检查是否可以执行下一曲操作
     * @return true 如果列表不为空
     */
    fun canNext(): Boolean = songs.isNotEmpty()

    /**
     * 检查是否可以执行上一曲操作
     * @return true 如果列表不为空
     */
    fun canPrevious(): Boolean = songs.isNotEmpty()
}
