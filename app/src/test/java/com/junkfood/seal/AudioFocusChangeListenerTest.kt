package com.junkfood.seal

import android.media.AudioManager
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.videolan.libvlc.MediaPlayer

class AudioFocusChangeListenerTest {

    private lateinit var listener: AudioFocusChangeListener
    private val mediaPlayer: MediaPlayer = mock()

    @Before
    fun setUp() {
        listener = AudioFocusChangeListener(mediaPlayer)
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_GAIN should play`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        verify(mediaPlayer).volume = 100
        verify(mediaPlayer).play()
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_LOSS should stop`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS)
        verify(mediaPlayer).stop()
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_LOSS_TRANSIENT should pause`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        verify(mediaPlayer).pause()
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK should lower volume`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        verify(mediaPlayer).volume = 30
    }
}
