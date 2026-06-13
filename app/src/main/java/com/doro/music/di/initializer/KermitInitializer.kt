package com.doro.music.di.initializer

import android.content.Context
import androidx.startup.Initializer
import co.touchlab.kermit.LogcatWriter
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.doro.music.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

class KermitInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Logger.setLogWriters(listOf(LogcatWriter(), FirebaseCrashlyticsLogWriter()))
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(FirebaseInitializer::class.java)

    private class FirebaseCrashlyticsLogWriter(
        private val minSeverity: Severity = Severity.Warn
    ) : LogWriter() {

        override fun isLoggable(tag: String, severity: Severity): Boolean =
            severity.ordinal >= minSeverity.ordinal

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log("version = ${BuildConfig.VERSION_NAME} type = ${BuildConfig.BUILD_TYPE}   [$severity] $tag: $message")
            throwable?.let { crashlytics.recordException(it) }
        }
    }
}
