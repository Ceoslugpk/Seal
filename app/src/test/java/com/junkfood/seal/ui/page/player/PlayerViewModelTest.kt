package com.junkfood.seal.ui.page.player

import android.content.Context
import app.cash.turbine.test
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer

@ExperimentalCoroutinesApi
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PlayerViewModel
    private val mediaPlayer: MediaPlayer = mock()
    private val context: Context = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PlayerViewModel(mediaPlayerFactory = { mediaPlayer })
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `togglePlayPause should toggle isPlaying state`() = runTest {
        viewModel.initializePlayer(context, "fake_path")
        testDispatcher.scheduler.advanceUntilIdle()

        whenever(mediaPlayer.isPlaying).thenReturn(true)

        viewModel.isPlaying.test {
            assertEquals(true, awaitItem())
            viewModel.togglePlayPause()
            assertEquals(false, awaitItem())
            viewModel.togglePlayPause()
            assertEquals(true, awaitItem())
        }

        verify(mediaPlayer).pause()
        verify(mediaPlayer).play()
    }

    @Test
    fun `seek should update media player time`() = runTest {
        viewModel.initializePlayer(context, "fake_path")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.seek(1000L)
        verify(mediaPlayer).time = 1000L
    }

    @Test
    fun `seekTo should update media player time`() = runTest {
        viewModel.initializePlayer(context, "fake_path")
        testDispatcher.scheduler.advanceUntilIdle()

        whenever(mediaPlayer.time).thenReturn(5000L)
        whenever(mediaPlayer.length).thenReturn(10000L)
        viewModel.seekTo(1000L)
        verify(mediaPlayer).time = 6000L
    }

    @Test
    fun `changeVolume should update media player volume`() = runTest {
        viewModel.initializePlayer(context, "fake_path")
        testDispatcher.scheduler.advanceUntilIdle()

        whenever(mediaPlayer.volume).thenReturn(50)
        viewModel.changeVolume(10)
        verify(mediaPlayer).volume = 60
    }

    @Test
    fun `changePlaybackRate should cycle through playback rates`() = runTest {
        viewModel.initializePlayer(context, "fake_path")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.playbackRate.test {
            assertEquals(1.0f, awaitItem())
            viewModel.changePlaybackRate()
            assertEquals(1.5f, awaitItem())
            viewModel.changePlaybackRate()
            assertEquals(2.0f, awaitItem())
            viewModel.changePlaybackRate()
            assertEquals(1.0f, awaitItem())
        }

        verify(mediaPlayer).rate = 1.5f
        verify(mediaPlayer).rate = 2.0f
        verify(mediaPlayer).rate = 1.0f
    }

    @Test
    fun `toggleAudioOnly should toggle isAudioOnly state`() = runTest {
        viewModel.isAudioOnly.test {
            assertEquals(false, awaitItem())
            viewModel.toggleAudioOnly()
            assertEquals(true, awaitItem())
            viewModel.toggleAudioOnly()
            assertEquals(false, awaitItem())
        }
    }
}
