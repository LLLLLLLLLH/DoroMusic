package com.doro.music.ext

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.safePop() {
    if (size > 1) removeLastOrNull()
}
