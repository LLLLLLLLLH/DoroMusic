package com.doro.music.domain

import com.doro.music.data.model.LyricsData
import com.doro.music.data.model.Song
import com.doro.music.data.repo.LyricsRepo

/**
 * 获取歌词用例
 */
class GetLyricsUseCase(private val lyricsRepo: LyricsRepo) {

    suspend operator fun invoke(song: Song): LyricsData? {
        return lyricsRepo.getLyrics(song)
    }
}
