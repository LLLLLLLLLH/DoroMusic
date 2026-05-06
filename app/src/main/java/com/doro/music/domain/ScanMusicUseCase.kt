package com.doro.music.domain

import com.doro.music.data.repo.MainRepo

class ScanMusicUseCase(private val repo: MainRepo) {

    suspend operator fun invoke() = repo.scan()
}