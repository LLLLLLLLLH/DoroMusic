package com.doro.music.ext

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri

fun Context.getAppVersionName(defaultVersion: String = "1.0"): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: defaultVersion
    } catch (_: PackageManager.NameNotFoundException) {
        defaultVersion
    }
}

fun Context.openUrl(url: String, @StringRes errorMessageRes: Int? = null) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    val activities = packageManager.queryIntentActivities(intent, 0)
    if (activities.isNotEmpty()) {
        startActivity(Intent.createChooser(intent, "Choose browser"))
    }else{
        errorMessageRes?.let { Toast.makeText(this, getString(it), Toast.LENGTH_SHORT).show() }
    }
}
