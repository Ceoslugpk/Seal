package com.junkfood.seal

import android.media.AudioManager
import androidx.media3.exoplayer.ExoPlayer
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AudioFocusChangeListenerTest {

    private lateinit var listener: AudioFocusChangeListener
    private val exoPlayer: ExoPlayer = mock()

    @Before
    fun setUp() {
        listener = AudioFocusChangeListener(exoPlayer)
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_GAIN should play`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        verify(exoPlayer).volume = 1.0f
        verify(exoPlayer).play()
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_LOSS should stop`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS)
        verify(exoPlayer).stop()
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_LOSS_TRANSIENT should pause`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        verify(exoPlayer).pause()
    }

    @Test
    fun `onAudioFocusChange with AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK should lower volume`() {
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        verify(exoPlayer).volume = 0.3f
    }
}
