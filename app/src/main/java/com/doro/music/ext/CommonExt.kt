package com.doro.music.ext

fun <T> T?.orDefault(default:T) = this ?: default