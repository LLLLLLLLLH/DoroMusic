package com.doro.music.di.initializer

import android.content.Context
import androidx.startup.Initializer
import com.doro.music.di.databaseModule
import com.doro.music.di.datastoreModule
import com.doro.music.di.playerModule
import com.doro.music.di.repoModule
import com.doro.music.di.useCaseModule
import com.doro.music.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

class KoinInitializer : Initializer<KoinApplication> {

    override fun create(context: Context): KoinApplication {
        return startKoin {
            androidContext(context)
            modules(databaseModule, datastoreModule, playerModule, repoModule, useCaseModule, viewModelModule)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
