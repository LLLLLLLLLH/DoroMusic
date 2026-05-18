package com.doro.music.base

import com.doro.music.data.model.DisplayMode
import com.doro.music.data.model.SortMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    /** Concrete subclass for testing the abstract BaseViewModel */
    private class TestBaseViewModel : BaseViewModel()

    @Test
    fun `default sortMode is TITLE`() {
        val vm = TestBaseViewModel()
        assertEquals(SortMode.TITLE, vm.sortMode.value)
    }

    @Test
    fun `default displayMode is LIST`() {
        val vm = TestBaseViewModel()
        assertEquals(DisplayMode.LIST, vm.displayMode.value)
    }

    @Test
    fun `setSortBy updates sortMode`() = runTest {
        val vm = TestBaseViewModel()
        vm.setSortBy(SortMode.ARTIST)
        assertEquals(SortMode.ARTIST, vm.sortMode.value)
    }

    @Test
    fun `setDisplayMode updates displayMode`() = runTest {
        val vm = TestBaseViewModel()
        vm.setDisplayMode(DisplayMode.GRID)
        assertEquals(DisplayMode.GRID, vm.displayMode.value)
    }

    @Test
    fun `setSortBy can be called multiple times`() = runTest {
        val vm = TestBaseViewModel()
        vm.setSortBy(SortMode.ARTIST)
        assertEquals(SortMode.ARTIST, vm.sortMode.value)
        vm.setSortBy(SortMode.DATE_ADDED)
        assertEquals(SortMode.DATE_ADDED, vm.sortMode.value)
        vm.setSortBy(SortMode.TITLE)
        assertEquals(SortMode.TITLE, vm.sortMode.value)
    }

    @Test
    fun `setDisplayMode can be called multiple times`() = runTest {
        val vm = TestBaseViewModel()
        vm.setDisplayMode(DisplayMode.GRID)
        assertEquals(DisplayMode.GRID, vm.displayMode.value)
        vm.setDisplayMode(DisplayMode.COMPACT)
        assertEquals(DisplayMode.COMPACT, vm.displayMode.value)
    }
}