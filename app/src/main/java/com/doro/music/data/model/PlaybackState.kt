package com.doro.music.data.model

enum class PlaybackState {
    IDLE,       // 初始状态，或播放列表为空
    PLAYING,    // 正在播放，显示暂停按钮
    PAUSED,     // 已暂停，显示播放按钮
    ERROR       // 播放出错
}