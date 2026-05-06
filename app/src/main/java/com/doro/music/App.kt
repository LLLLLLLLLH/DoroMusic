package com.doro.music

import android.app.Application
import com.doro.music.di.databaseModule
import com.doro.music.di.datastoreModule
import com.doro.music.di.repoModule
import com.doro.music.di.useCaseModule
import com.doro.music.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(databaseModule, datastoreModule, repoModule, useCaseModule, viewModelModule)
        }
    }
}
