package com.doro.music.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val path: String,
    val songCount: Int = 0,
    val excluded: Boolean = false
) {
    val name: String
        get() = path.substringAfterLast('/')
}

fun List<Folder>.activeFolders() = filterNot { it.excluded }
fun List<Folder>.excludedFolders() = filter { it.excluded }
