package com.doro.music.domain

import com.doro.music.data.model.Song
import com.doro.music.data.model.SortMode
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.model.PlayAction
import com.doro.music.player.model.PlayContext
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PlaybackUseCaseTest {

    private val mockDispatcher = mockk<PlayActionDispatcher>(relaxed = true)
    private lateinit var useCase: PlaybackUseCase

    @Before
    fun setup() {
        useCase = PlaybackUseCase(mockDispatcher)
    }

    @Test
    fun `play dispatches Play action`() {
        val song = Song(id = 10L, title = "Song", path = "/song.mp3")
        val context = PlayContext.All(SortMode.TITLE)

        useCase.play(song, context)

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play &&
                    action.songId == 10L &&
                    action.playContext == context
            })
        }
    }

    @Test
    fun `play with null song does not dispatch`() {
        useCase.play(null, PlayContext.All(SortMode.TITLE))

        verify(exactly = 0) { mockDispatcher.dispatch(any()) }
    }

    @Test
    fun `playFirst dispatches first song`() {
        val songs = listOf(
            Song(id = 1L, title = "First", path = "/first.mp3"),
            Song(id = 2L, title = "Second", path = "/second.mp3")
        )

        useCase.playFirst(songs, PlayContext.All(SortMode.TITLE))

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.Play && action.songId == 1L
            })
        }
    }

    @Test
    fun `playFirst with empty list does not dispatch`() {
        useCase.playFirst(emptyList(), PlayContext.All(SortMode.TITLE))

        verify(exactly = 0) { mockDispatcher.dispatch(any()) }
    }

    @Test
    fun `shufflePlay with empty list does not dispatch`() {
        useCase.shufflePlay(emptyList(), PlayContext.All(SortMode.TITLE))

        verify(exactly = 0) { mockDispatcher.dispatch(any()) }
    }

    @Test
    fun `addToNext dispatches InsertSingle action`() {
        val song = Song(id = 20L, title = "Song", path = "/song.mp3")

        useCase.addToNext(song)

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.InsertSingle && action.songId == 20L
            })
        }
    }

    @Test
    fun `addGroupToNext dispatches InsertGroup action`() {
        val context = PlayContext.Playlist(5L, SortMode.DATE_ADDED)

        useCase.addGroupToNext(context)

        verify {
            mockDispatcher.dispatch(match { action ->
                action is PlayAction.InsertGroup && action.playContext == context
            })
        }
    }
}
