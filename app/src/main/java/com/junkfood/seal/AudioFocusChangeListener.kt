package com.junkfood.seal

import android.media.AudioManager
import androidx.media3.exoplayer.ExoPlayer

class AudioFocusChangeListener(
    private val exoPlayer: ExoPlayer
) : AudioManager.OnAudioFocusChangeListener {
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer.volume = 1.0f
                exoPlayer.play()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                exoPlayer.stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                exoPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                exoPlayer.volume = 0.3f
            }
        }
    }
}
