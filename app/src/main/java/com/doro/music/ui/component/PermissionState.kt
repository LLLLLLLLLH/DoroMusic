package com.doro.music.ui.component

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Stable
class AudioPermissionState internal constructor(
    hasPermission: Boolean,
    private val requestAction: () -> Unit
) {

    var hasPermission by mutableStateOf(hasPermission)
        internal set

    fun request() = requestAction()
}

@Composable
fun rememberAudioPermissionState(
    onGranted: () -> Unit = {},
    onDenied: (isPermanentlyDenied: Boolean) -> Unit = {}
): AudioPermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnGranted by rememberUpdatedState(onGranted)
    val currentOnDenied by rememberUpdatedState(onDenied)

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun checkPermission(): Boolean = requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    var hasPermission by rememberSaveable { mutableStateOf(checkPermission()) }
    val currentPermission = checkPermission()
    if (currentPermission != hasPermission) {
        hasPermission = currentPermission
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission) {
            currentOnGranted()
        } else {
            val activity = context as? ComponentActivity
            val permanentlyDenied = requiredPermissions.any { permission ->
                permissions.containsKey(permission) && !permissions[permission]!! &&
                    activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
            currentOnDenied(permanentlyDenied)
        }
    }

    val state = remember {
        AudioPermissionState(hasPermission) {
            if (!hasPermission) {
                launcher.launch(requiredPermissions)
            } else {
                currentOnGranted()
            }
        }
    }
    state.hasPermission = hasPermission

    LaunchedEffect(Unit) {
        if (!hasPermission) state.request()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val nowHasPermission = checkPermission()
                if (nowHasPermission && !hasPermission) {
                    hasPermission = true
                    currentOnGranted()
                } else if (!nowHasPermission && hasPermission) {
                    hasPermission = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return state
}
