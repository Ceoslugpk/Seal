package com.junkfood.seal

import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.videolan.libvlc.MediaPlayer

class MediaSessionCallbackTest {

    private lateinit var callback: MediaSessionCallback
    private val mediaPlayer: MediaPlayer = mock()
    private val service: MediaPlaybackService = mock()

    @Before
    fun setUp() {
        callback = MediaSessionCallback(mediaPlayer, service)
    }

    @Test
    fun `onPlay should request audio focus and call mediaPlayer play`() {
        callback.onPlay()
        verify(service).requestAudioFocus()
        verify(mediaPlayer).play()
    }

    @Test
    fun `onPause should call mediaPlayer pause`() {
        callback.onPause()
        verify(mediaPlayer).pause()
    }

    @Test
    fun `onStop should call mediaPlayer stop, abandon audio focus and stopSelf`() {
        callback.onStop()
        verify(mediaPlayer).stop()
        verify(service).abandonAudioFocus()
        verify(service).stopSelf()
    }
}
