package com.doro.music.di

import androidx.room.Room
import com.doro.music.data.datastore.PlayerStateDataStore
import com.doro.music.data.datastore.SettingsDataStore
import com.doro.music.data.db.AppDataBase
import com.doro.music.data.repo.ArtistRepo
import com.doro.music.data.repo.FolderRepo
import com.doro.music.data.repo.MainRepo
import com.doro.music.data.repo.PlayQueueRepo
import com.doro.music.data.repo.PlayQueueStore
import com.doro.music.data.repo.PlaylistRepo
import com.doro.music.data.repo.SearchRepo
import com.doro.music.data.repo.SettingsRepo
import com.doro.music.data.repo.SongListRepo
import com.doro.music.data.repo.SongRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.domain.GetSongFoldersUseCase
import com.doro.music.domain.PlaySongsUseCase
import com.doro.music.domain.ScanMusicUseCase
import com.doro.music.data.repo.PlaybackRepository
import com.doro.music.data.repo.PlaybackStateSaver
import com.doro.music.player.PlaybackController
import com.doro.music.player.MusicScanner
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
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDataBase::class.java,
            "doro_music"
        ).fallbackToDestructiveMigration(false)
            .addMigrations(AppDataBase.MIGRATION_8_9)
            .build()
    }
    single { get<AppDataBase>().songDao() }
    single { get<AppDataBase>().playlistDao() }
    single { get<AppDataBase>().playlistSongDao() }
    single { get<AppDataBase>().artistDao() }
    single { get<AppDataBase>().playQueueDao() }
    single { get<AppDataBase>().searchDao() }
    single { get<AppDataBase>().folderDao() }
}

val datastoreModule = module {
    single { PlayerStateDataStore(androidContext()) }
    single { SettingsDataStore(androidContext()) }
}

val repoModule = module {
    single { SongRepo(get()) }
    single { PlaylistRepo(get(), get()) }
    single { ArtistRepo(get()) }
    single { FolderRepo(get(), get()) }
    single { MusicScanner(androidContext()) }
    single { MainRepo(get(), get(),get(),get()) }
    single { SongListRepo(get(), get()) }
    single<PlayQueueStore> { PlayQueueRepo(get()) }
    single { SearchRepo(get()) }
    single { SettingsRepo(get()) }
    single { PlaybackController(androidContext()) }
    single { PlaybackStateSaver(get(), get()) }
    single { PlaybackRepository(get(), get()) }
}

val useCaseModule = module {
    single { GetPlaylistsUseCase(get()) }
    single { AddSongToPlaylistUseCase(get()) }
    single { GetSongFoldersUseCase(get()) }
    single { ScanMusicUseCase(get()) }
    single { PlaySongsUseCase(get()) }
}

val viewModelModule = module {
    viewModel { SongsViewModel(get(), get(), get(), get(), get()) }
    viewModel { PlaylistsViewModel(get(), get(), get()) }
    viewModel { ArtistsViewModel(get(), get(), get()) }
    viewModel { FoldersViewModel(get(), get(), get(), get(), get()) }
    viewModel { MainViewModel(get()) }
    viewModel { SongListViewModel(get(), get(), get(), get(), get()) }
    viewModel { PlayerViewModel(get()) }
    viewModel { SearchViewModel(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(),get(),get()) }
    viewModel { MainActivityViewModel(get()) }
}
