package com.doro.music.di.initializer

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        FirebaseApp.initializeApp(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
