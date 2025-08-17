package com.junkfood.seal

import android.support.v4.media.session.MediaSessionCompat
import org.videolan.libvlc.MediaPlayer

class MediaSessionCallback(
    private val mediaPlayer: MediaPlayer,
    private val service: MediaPlaybackService
) : MediaSessionCompat.Callback() {
    override fun onPlay() {
        service.requestAudioFocus()
        mediaPlayer.play()
    }

    override fun onPause() {
        mediaPlayer.pause()
    }

    override fun onStop() {
        mediaPlayer.stop()
        service.abandonAudioFocus()
        service.stopSelf()
    }
}
