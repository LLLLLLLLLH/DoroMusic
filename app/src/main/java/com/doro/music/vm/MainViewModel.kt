package com.doro.music.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doro.music.domain.ScanMusicUseCase
import com.doro.music.player.ScanResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val scanMusicUseCase: ScanMusicUseCase,
) : ViewModel() {

    val scanState: StateFlow<ScanState>
        field = MutableStateFlow<ScanState>(ScanState.Idle)

    val scanEvent: SharedFlow<ScanState>
        field = MutableSharedFlow<ScanState>()

    fun scan() {
        viewModelScope.launch {
            scanState.emit(ScanState.Scanning)

            when (val result = scanMusicUseCase()) {
                is ScanResult.Success -> ScanState.Done(result.songs.size)
                is ScanResult.Error -> ScanState.Error
            }.also {
                scanEvent.emit(it)
                scanState.emit(ScanState.Idle)
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
