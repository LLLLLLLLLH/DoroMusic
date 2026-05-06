package com.doro.music.domain

import com.doro.music.data.repo.FolderRepo

class GetSongFoldersUseCase(private val repo: FolderRepo) {

    operator fun invoke() = repo.folders
}
