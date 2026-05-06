package com.doro.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import com.doro.music.R

private val placeholders: List<Int> = listOf(
    R.drawable.img_album_01,
    R.drawable.img_album_02,
    R.drawable.img_album_03,
    R.drawable.img_album_04,
    R.drawable.img_album_05,
    R.drawable.img_album_06,
    R.drawable.img_album_07,
    R.drawable.img_album_08,
)

private val defaultPlaceholder by lazy { placeholders[placeholders.indices.random()] }

@Composable
fun placeholderPainter() = painterResource(defaultPlaceholder)
