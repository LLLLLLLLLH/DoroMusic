# PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-18
**Commit:** 3b49125
**Branch:** master

## OVERVIEW

DoroMusic is an Android music player built with Kotlin, Jetpack Compose, Room, Koin DI, Media3/ExoPlayer, and Navigation 3. It uses Clean Architecture with a single-Activity, Compose-first UI.

## STRUCTURE

```
DoroMusic/
├── app/src/main/java/com/doro/music/
│   ├── App.kt                    # Application entry point (Koin init)
│   ├── base/                     # BaseViewModel (sort, display mode, UI events)
│   ├── data/                     # Data layer (Room, DataStore, repositories)
│   │   ├── datastore/            # Preferences (Settings, PlayState, PlayerState)
│   │   ├── db/                   # Room database + DAOs + entities
│   │   ├── model/                # Domain models (Song, Playlist, SortMode, etc.)
│   │   └── repo/                 # Repository implementations (Paging + Flow)
│   ├── di/                       # Koin modules (database, player, repo, useCase, viewModel)
│   ├── domain/                   # Use cases (ScanMusic, AddSongToPlaylist, etc.)
│   ├── ext/                      # Kotlin extension functions
│   ├── player/                   # Playback system
│   │   ├── controller/           # MediaPlaybackController (ExoPlayer wrapper)
│   │   ├── model/                # PlayAction, PlayUiState, QueueSong
│   │   ├── service/              # PlayerService (MediaSessionService)
│   │   └── util/                 # MusicScanner
│   ├── ui/                       # Presentation layer
│   │   ├── component/            # Reusable Compose components
│   │   ├── screen/               # Screens + Navigation 3 routes
│   │   └── theme/                # Material 3 theme (dynamic color supported)
│   └── vm/                       # ViewModels (one per screen)
├── app/src/main/res/             # Android resources
├── gradle/libs.versions.toml     # Version catalog
└── .github/workflows/ci.yml      # GitHub Actions CI
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Add a new screen | `ui/screen/` | Add route in `AppNav.kt`, create ViewModel in `vm/` |
| Change playback behavior | `player/PlayerSession.kt` | Core dispatcher/observer/connector |
| Add database entity | `data/db/entities/` | Add DAO in `data/db/dao/`, register in `AppDataBase.kt` |
| Add repository | `data/repo/` | Inject DAO, expose Flow/Paging |
| Add DI binding | `di/AppModule.kt` | Koin modules: database, datastore, player, repo, useCase, viewModel |
| Change theme | `ui/theme/` | Color.kt, Type.kt, Theme.kt (supports dynamic color) |
| Scan local music | `player/util/MusicScanner.kt` | Triggered via `ScanMusicUseCase` |

## CODE MAP

| Symbol | Type | Location | Role |
|--------|------|----------|------|
| `App` | class | `App.kt` | Application entry, Koin start |
| `MainActivity` | class | `ui/MainActivity.kt` | Single Activity, edge-to-edge, theme observer |
| `AppNav` | @Composable | `ui/screen/AppNav.kt` | Navigation 3 root (slide transitions) |
| `PlayerSession` | class | `player/PlayerSession.kt` | Playback core: dispatcher, observer, connector |
| `PlayerService` | class | `player/service/PlayerService.kt` | MediaSessionService for background playback |
| `BaseViewModel` | abstract class | `base/BaseViewModel.kt` | Shared sort/display mode + UI events |
| `AppDataBase` | abstract class | `data/db/AppDataBase.kt` | Room database with migrations |
| `AppModule` | object | `di/AppModule.kt` | All Koin modules (database, player, repo, VM) |

## CONVENTIONS

- **Kotlin code style**: `official` (gradle.properties)
- **Architecture**: Clean Architecture (`data` → `domain` → `ui/vm`)
- **DI**: Koin with module separation (`databaseModule`, `playerModule`, `repoModule`, `useCaseModule`, `viewModelModule`)
- **UI state**: `StateFlow` / `SharedFlow` in ViewModels, collected with `collectAsStateWithLifecycle`
- **Database**: Room with KSP, DAOs return `Flow` or Paging `PagingSource`
- **Navigation**: Navigation 3 (`androidx.navigation3`) with custom `entryProvider` and slide transitions
- **Image loading**: Coil3 (`coil-compose`)
- **Serialization**: kotlinx.serialization for navigation arguments
- **Paging**: `androidx.paging` for song/artist/playlist lists

## ANTI-PATTERNS (THIS PROJECT)

- **No explicit anti-pattern comments** found in source code
- **Security note**: `app/build.gradle.kts` has hardcoded fallback keystore passwords (`doro123456`) — use env vars in CI
- **No formal README or coding guidelines** exist yet

## UNIQUE STYLES

- **Single-song preload**: `PlayerSession` preloads only current + next song; Room is the queue source of truth
- **Play mode zero-cost switch**: switching REPEAT/SHUFFLE/REPEAT_ONE only updates `playMode` in DB, does not touch ExoPlayer queue
- **Mutex-guarded navigation**: `handleNext()` and `handlePrev()` use `navigationMutex` to prevent race conditions
- **Custom compiler args**: `-Xexplicit-backing-fields` and `-XXLanguage:+PropertyParamAnnotationDefaultTargetMode`

## COMMANDS

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install debug
./gradlew installDebug
```

## NOTES

- **minSdk**: 24, **targetSdk/compileSdk**: 36 (with minorApiLevel 1)
- **Java compatibility**: 11
- **Compose BOM**: 2026.03.01 (bleeding-edge)
- **CI**: GitHub Actions on `ubuntu-latest`, JDK 17, builds debug + runs unit tests
- **IDE inspections**: Compose preview rules are set to ERROR level (must be top-level, must have `@Composable`)
- **ProGuard**: Enabled on release; keeps Room entities, kotlinx.serialization classes, Media3/ExoPlayer
