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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _sortMode = MutableStateFlow<SortMode>(getDefaultSortMode())
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _displayMode = MutableStateFlow<DisplayMode>(DisplayMode.LIST)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun setSortBy(sort: SortMode) {
        _sortMode.tryEmit(sort)
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.tryEmit(mode)
    }

    protected fun emitEvent(event: UiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }

    protected suspend fun <T> safeCall(
        onError: (Throwable) -> Unit = {},
        block: suspend () -> T
    ): Result<T> = runCatching { block() }
        .onFailure { onError(it) }

    protected open fun getDefaultSortMode(): SortMode = SortMode.TITLE

}
