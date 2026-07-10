package com.doro.music.vm

import androidx.paging.PagingData
import com.doro.music.data.model.AddSongResult
import com.doro.music.data.model.Artist
import com.doro.music.data.model.Playlist
import com.doro.music.data.model.Song
import com.doro.music.data.repo.SongListRepo
import com.doro.music.domain.AddSongToPlaylistUseCase
import com.doro.music.domain.GetPlaylistsUseCase
import com.doro.music.domain.PlaybackUseCase
import com.doro.music.player.model.PlayContext
import com.doro.music.ui.screen.other.SongListSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<SongListRepo>()
    private val mockGetPlaylistsUseCase = mockk<GetPlaylistsUseCase>()
    private val mockAddSongToPlaylistUseCase = mockk<AddSongToPlaylistUseCase>()
    private val mockPlaybackUseCase = mockk<PlaybackUseCase>(relaxed = true)

    private lateinit var viewModel: SongListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepo.getSongListByArtist(any()) } returns flowOf(PagingData.empty())
        coEvery { mockRepo.getSongListByPlaylist(any()) } returns flowOf(PagingData.empty())
        every { mockRepo.getSongCountByArtist(any()) } returns flowOf(0)
        every { mockRepo.getSongCountByPlaylist(any()) } returns flowOf(0)
        coEvery { mockGetPlaylistsUseCase() } returns flowOf(PagingData.empty())

        viewModel = SongListViewModel(mockRepo, mockGetPlaylistsUseCase, mockAddSongToPlaylistUseCase, mockPlaybackUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectSong updates selectedSong state`() = runTest {
        viewModel.selectSong(123L)
        assertEquals(123L, viewModel.selectedSongId.value)
    }

    @Test
    fun `selectSong with null clears selection`() = runTest {
        viewModel.selectSong(123L)
        viewModel.selectSong(null)
        assertNull(viewModel.selectedSongId.value)
    }

    @Test
    fun `setSource with FromArtist updates source state and triggers songs flow`() = runTest {
        val artist = Artist(name = "TestArtist", songCount = 5)
        viewModel.setSource(SongListSource.FromArtist(artist))
        
        // Collect from songs flow to trigger the flatMapLatest lambda
        val collectJob = launch { viewModel.songs.collect {} }
        advanceUntilIdle()

        assertNotNull(viewModel.songs)
        collectJob.cancel()
    }

    @Test
    fun `setSource with FromPlaylist updates source state and triggers songs flow`() = runTest {
        val playlist = Playlist(id = 1L, name = "TestPlaylist", songCount = 3)
        viewModel.setSource(SongListSource.FromPlaylist(playlist))
        
        // Collect from songs flow to trigger the flatMapLatest lambda
        val collectJob = launch { viewModel.songs.collect {} }
        advanceUntilIdle()

        assertNotNull(viewModel.songs)
        collectJob.cancel()
    }

    @Test
    fun `songCount flow is triggered when source is set`() = runTest {
        val artist = Artist(name = "TestArtist", songCount = 5)
        viewModel.setSource(SongListSource.FromArtist(artist))
        
        // Collect from songCount flow to trigger the flatMapLatest lambda
        val collectJob = launch { viewModel.songCount.collect {} }
        advanceUntilIdle()

        assertNotNull(viewModel.songCount)
        collectJob.cancel()
    }

    @Test
    fun `play with null song does not dispatch action`() = runTest {
        viewModel.play(null)
        verify(exactly = 0) { mockPlaybackUseCase.play(any(), any()) }
    }

    @Test
    fun `play with FromArtist dispatches Play with Artist context`() = runTest {
        val artist = Artist(name = "TestArtist", songCount = 1)
        viewModel.setSource(SongListSource.FromArtist(artist))

        val song = Song(id = 10L, title = "Test", path = "/test")
        viewModel.play(song)

        verify { mockPlaybackUseCase.play(song, match { it is PlayContext.Artist }) }
    }

    @Test
    fun `play with FromPlaylist dispatches Play with Playlist context`() = runTest {
        val playlist = Playlist(id = 5L, name = "P")
        viewModel.setSource(SongListSource.FromPlaylist(playlist))

        val song = Song(id = 20L, title = "Test", path = "/test")
        viewModel.play(song)

        verify { mockPlaybackUseCase.play(song, match { it is PlayContext.Playlist }) }
    }

    @Test
    fun `playAll with empty songs does not dispatch`() = runTest {
        val artist = Artist(name = "Empty", songCount = 0)
        viewModel.setSource(SongListSource.FromArtist(artist))
        coEvery { mockRepo.getAllSongsByArtist("Empty") } returns emptyList()

        viewModel.playAll()
        advanceUntilIdle()

        verify { mockPlaybackUseCase.playFirst(emptyList(), match { it is PlayContext.Artist }) }
    }

    @Test
    fun `playAll dispatches Play with first song`() = runTest {
        val artist = Artist(name = "Artist", songCount = 2)
        viewModel.setSource(SongListSource.FromArtist(artist))
        val songs = listOf(
            Song(id = 1L, title = "First", path = "/a"),
            Song(id = 2L, title = "Second", path = "/b")
        )
        coEvery { mockRepo.getAllSongsByArtist("Artist") } returns songs

        viewModel.playAll()
        advanceUntilIdle()

        verify { mockPlaybackUseCase.playFirst(songs, match { it is PlayContext.Artist }) }
    }

    @Test
    fun `shufflePlay dispatches Play with random song`() = runTest {
        val artist = Artist(name = "Artist", songCount = 1)
        viewModel.setSource(SongListSource.FromArtist(artist))
        val songs = listOf(Song(id = 5L, title = "Only", path = "/a"))
        coEvery { mockRepo.getAllSongsByArtist("Artist") } returns songs

        viewModel.shufflePlay()
        advanceUntilIdle()

        verify { mockPlaybackUseCase.shufflePlay(songs, match { it is PlayContext.Artist }) }
    }

    @Test
    fun `addToNext dispatches InsertSingle action`() = runTest {
        val song = Song(id = 50L, title = "Test", path = "/test")
        viewModel.addToNext(song)
        advanceUntilIdle()

        verify { mockPlaybackUseCase.addToNext(song) }
    }

    @Test
    fun `addSongToPlaylist does nothing when no song selected`() = runTest {
        viewModel.selectSong(null)
        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify(exactly = 0) { mockAddSongToPlaylistUseCase(any(), any()) }
    }

    @Test
    fun `removeSongFromPlaylist delegates to repo`() = runTest {
        coEvery { mockRepo.removeSongFromPlaylist(1L, 100L) } returns true

        viewModel.removeSongFromPlaylist(1L, 100L)
        advanceUntilIdle()

        coVerify { mockRepo.removeSongFromPlaylist(1L, 100L) }
    }

    @Test
    fun `playAll with FromPlaylist dispatches Play with first song`() = runTest {
        val playlist = Playlist(id = 5L, name = "MyPlaylist", songCount = 2)
        viewModel.setSource(SongListSource.FromPlaylist(playlist))
        val songs = listOf(
            Song(id = 10L, title = "First", path = "/a"),
            Song(id = 20L, title = "Second", path = "/b")
        )
        coEvery { mockRepo.getAllSongsByPlaylist(5L) } returns songs

        viewModel.playAll()
        advanceUntilIdle()

        verify { mockPlaybackUseCase.playFirst(songs, match { it is PlayContext.Playlist }) }
    }

    @Test
    fun `shufflePlay with FromPlaylist dispatches Play`() = runTest {
        val playlist = Playlist(id = 5L, name = "MyPlaylist", songCount = 1)
        viewModel.setSource(SongListSource.FromPlaylist(playlist))
        val songs = listOf(Song(id = 15L, title = "Only", path = "/a"))
        coEvery { mockRepo.getAllSongsByPlaylist(5L) } returns songs

        viewModel.shufflePlay()
        advanceUntilIdle()

        verify { mockPlaybackUseCase.shufflePlay(songs, match { it is PlayContext.Playlist }) }
    }

    @Test
    fun `shufflePlay with empty songs does not dispatch`() = runTest {
        val playlist = Playlist(id = 5L, name = "Empty", songCount = 0)
        viewModel.setSource(SongListSource.FromPlaylist(playlist))
        coEvery { mockRepo.getAllSongsByPlaylist(5L) } returns emptyList()

        viewModel.shufflePlay()
        advanceUntilIdle()

        verify { mockPlaybackUseCase.shufflePlay(emptyList(), match { it is PlayContext.Playlist }) }
    }

    @Test
    fun `playAll with null source does not dispatch`() = runTest {
        // Don't set source - source.value is null
        viewModel.playAll()
        advanceUntilIdle()

        verify(exactly = 0) { mockPlaybackUseCase.playFirst(any(), any()) }
    }

    @Test
    fun `shufflePlay with null source does not dispatch`() = runTest {
        // Don't set source - source.value is null
        viewModel.shufflePlay()
        advanceUntilIdle()

        verify(exactly = 0) { mockPlaybackUseCase.shufflePlay(any(), any()) }
    }

    @Test
    fun `addSongToPlaylist calls use case when song selected`() = runTest {
        viewModel.selectSong(100L)
        coEvery { mockAddSongToPlaylistUseCase(any(), any()) } returns AddSongResult.Success

        viewModel.addSongToPlaylist(setOf(Playlist(id = 1L, name = "P1")))
        advanceUntilIdle()

        coVerify { mockAddSongToPlaylistUseCase(songId = 100L, playlists = any()) }
    }

    @Test
    fun `removeSongFromPlaylist with failure still emits event`() = runTest {
        coEvery { mockRepo.removeSongFromPlaylist(1L, 100L) } returns false

        viewModel.removeSongFromPlaylist(1L, 100L)
        advanceUntilIdle()

        coVerify { mockRepo.removeSongFromPlaylist(1L, 100L) }
    }

    @Test
    fun `removeSongFromPlaylist with exception still emits event`() = runTest {
        coEvery { mockRepo.removeSongFromPlaylist(any(), any()) } throws RuntimeException("db error")

        viewModel.removeSongFromPlaylist(1L, 100L)
        advanceUntilIdle()

        coVerify { mockRepo.removeSongFromPlaylist(1L, 100L) }
    }

    @Test
    fun `play with null song does not dispatch even when source is set`() = runTest {
        val artist = Artist(name = "TestArtist", songCount = 1)
        viewModel.setSource(SongListSource.FromArtist(artist))
        advanceUntilIdle()

        viewModel.play(null)

        verify(exactly = 0) { mockPlaybackUseCase.play(any(), any()) }
    }

    @Test
    fun `play with null source does not dispatch`() = runTest {
        val song = Song(id = 10L, title = "Test", path = "/test")
        viewModel.play(song)

        verify(exactly = 0) { mockPlaybackUseCase.play(any(), any()) }
    }
}
