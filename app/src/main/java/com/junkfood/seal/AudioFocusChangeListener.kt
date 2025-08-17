package com.junkfood.seal

import android.media.AudioManager
import org.videolan.libvlc.MediaPlayer

class AudioFocusChangeListener(
    private val mediaPlayer: MediaPlayer
) : AudioManager.OnAudioFocusChangeListener {
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer.volume = 100
                mediaPlayer.play()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaPlayer.stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer.volume = 30
            }
        }
    }
}
