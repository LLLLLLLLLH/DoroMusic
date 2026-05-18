package com.doro.music.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doro.music.domain.ScanMusicUseCase
import com.doro.music.player.util.ScanResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val scanMusicUseCase: ScanMusicUseCase,
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _scanEvent = MutableSharedFlow<ScanState>()
    val scanEvent: SharedFlow<ScanState> = _scanEvent.asSharedFlow()

    fun scan() {
        viewModelScope.launch {
            _scanState.emit(ScanState.Scanning)

            when (val result = scanMusicUseCase()) {
                is ScanResult.Success -> ScanState.Done(result.songs.size)
                is ScanResult.Error -> ScanState.Error
            }.also {
                _scanEvent.emit(it)
                _scanState.emit(ScanState.Idle)
            }
        }
    }

    sealed interface ScanState {
        data object Idle : ScanState
        data object Scanning : ScanState
        data object Error : ScanState
        data class Done(val count: Int) : ScanState
    }
}