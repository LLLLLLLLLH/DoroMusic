package com.doro.music.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.doro.music.data.model.DarkThemeMode
import com.doro.music.ui.screen.AppNav
import com.doro.music.ui.theme.DoroMusicTheme
import com.doro.music.vm.MainActivityViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val vm by viewModel<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.darkThemeMode.collectLatest { mode ->
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            lightScrim = android.graphics.Color.TRANSPARENT,
                            darkScrim = android.graphics.Color.TRANSPARENT,
                        ) { isDarkMode(mode) },
                        navigationBarStyle = SystemBarStyle.auto(
                            lightScrim = android.graphics.Color.TRANSPARENT,
                            darkScrim = android.graphics.Color.TRANSPARENT,
                        ) { isDarkMode(mode) },
                    )
                }
            }
        }

        setContent {
            val darkThemeMode by vm.darkThemeMode.collectAsStateWithLifecycle()
            val darkTheme = when (darkThemeMode) {
                DarkThemeMode.SYSTEM -> isSystemInDarkTheme()
                DarkThemeMode.LIGHT -> false
                DarkThemeMode.DARK -> true
            }

            DoroMusicTheme(darkTheme = darkTheme) {
                AppNav(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.disconnect()
    }

    private fun isDarkMode(mode: DarkThemeMode): Boolean = when (mode) {
        DarkThemeMode.SYSTEM -> {
            val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightMode == Configuration.UI_MODE_NIGHT_YES
        }

        DarkThemeMode.LIGHT -> false
        DarkThemeMode.DARK -> true
    }
}
