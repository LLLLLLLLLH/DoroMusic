package com.doro.music.di

import androidx.room.Room
import com.doro.music.data.datastore.SettingsDataStore
import com.doro.music.data.db.AppDataBase
import com.doro.music.data.repo.ArtistRepo
import com.doro.music.data.repo.FolderRepo
import com.doro.music.data.repo.MainRepo
import com.doro.music.data.repo.PlaylistRepo
import com.doro.music.data.repo.SearchRepo
import com.doro.music.data.repo.SettingsRepo
import com.doro.music.data.repo.SongListRepo
import com.doro.music.data.repo.SongRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.domain.GetSongFoldersUseCase
import com.doro.music.domain.ScanMusicUseCase
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.PlayerConnector
import com.doro.music.player.PlayerSession
import com.doro.music.player.PlayStateObserver
import com.doro.music.data.db.dao.PlayQueueDao
import com.doro.music.data.repo.PlayQueueRepo
import com.doro.music.data.repo.QueueReadOps
import com.doro.music.data.repo.QueueWriteOps
import com.doro.music.player.util.MusicScanner
import com.doro.music.data.datastore.PlayStateDataStoreImpl
import com.doro.music.data.datastore.PlayStateDataStore
import com.doro.music.vm.ArtistsViewModel
import com.doro.music.vm.FoldersViewModel
import com.doro.music.vm.MainActivityViewModel
import com.doro.music.vm.MainViewModel
import com.doro.music.vm.PlayerViewModel
import com.doro.music.vm.PlaylistsViewModel
import com.doro.music.vm.SearchViewModel
import com.doro.music.vm.SettingsViewModel
import com.doro.music.vm.SongListViewModel
import com.doro.music.vm.SongsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.binds
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDataBase::class.java,
            "doro_music"
        ).fallbackToDestructiveMigration(false)
            .addMigrations(AppDataBase.MIGRATION_8_9)
            .addMigrations(AppDataBase.MIGRATION_9_10)
            .build()
    }
    single { get<AppDataBase>().songDao() }
    single { get<AppDataBase>().playlistDao() }
    single { get<AppDataBase>().playlistSongDao() }
    single { get<AppDataBase>().artistDao() }
    single { get<AppDataBase>().searchDao() }
    single { get<AppDataBase>().folderDao() }
}

val datastoreModule = module {
    single { SettingsDataStore(androidContext()) }
}

val playerModule = module {
    single<PlayQueueDao> { get<AppDataBase>().playQueueDao() }

    single<PlayStateDataStore> { PlayStateDataStoreImpl(androidContext()) }

    single { PlayQueueRepo(get(), get(), get(), get(), get()) }.binds(
        arrayOf(
            QueueWriteOps::class,
            QueueReadOps::class
        )
    )

    single {
        PlayerSession(
            context = androidContext(),
            queueOps = get<QueueWriteOps>(),
            queueReadOps = get<QueueReadOps>(),
            stateDataStore = get<PlayStateDataStore>()
        )
    }.binds(
        arrayOf(
            PlayActionDispatcher::class,
            PlayStateObserver::class,
            PlayerConnector::class
        )
    )

    single { MusicScanner(androidContext()) }
}

val repoModule = module {
    single { SongRepo(get()) }
    single { PlaylistRepo(get(), get()) }
    single { ArtistRepo(get()) }
    single { FolderRepo(get(), get()) }
    single { MainRepo(get(), get(), get(), get()) }
    single { SongListRepo(get(), get()) }
    single { SearchRepo(get()) }
    single { SettingsRepo(get()) }
}

val useCaseModule = module {
    factory { GetPlaylistsUseCase(get()) }
    factory { AddSongToPlaylistUseCase(get()) }
    factory { GetSongFoldersUseCase(get()) }
    factory { ScanMusicUseCase(get()) }
}

val viewModelModule = module {
    viewModel { SongsViewModel(get(), get(), get(), get()) }
    viewModel { PlaylistsViewModel(get(), get()) }
    viewModel { ArtistsViewModel(get(), get()) }
    viewModel { FoldersViewModel(get(), get(), get(), get()) }
    viewModel { MainViewModel(get()) }
    viewModel { SongListViewModel(get(), get(), get(), get()) }
    viewModel { PlayerViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
    viewModel { MainActivityViewModel(get(), get()) }
}