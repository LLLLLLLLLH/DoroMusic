package com.doro.music.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doro.music.data.model.DisplayMode
import com.doro.music.data.model.SortMode
import com.doro.music.data.model.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    val sortMode: StateFlow<SortMode>
        field = MutableStateFlow<SortMode>(getDefaultSortMode())

    val displayMode: StateFlow<DisplayMode>
        field = MutableStateFlow<DisplayMode>(DisplayMode.LIST)

    val uiEvent: SharedFlow<UiEvent>
        field = MutableSharedFlow<UiEvent>()

    fun setSortBy(sort: SortMode) {
        sortMode.tryEmit(sort)
    }

    fun setDisplayMode(mode: DisplayMode) {
        displayMode.tryEmit(mode)
    }

    protected fun emitEvent(event: UiEvent) {
        viewModelScope.launch { uiEvent.emit(event) }
    }

    protected suspend fun <T> safeCall(
        onError: (Throwable) -> Unit = {},
        block: suspend () -> T
    ): Result<T> = runCatching { block() }
        .onFailure { onError(it) }

    protected open fun getDefaultSortMode(): SortMode = SortMode.TITLE

}
