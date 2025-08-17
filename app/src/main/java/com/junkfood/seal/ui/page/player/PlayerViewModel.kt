package com.junkfood.seal.ui.page.player

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.junkfood.seal.MediaPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel() : ViewModel() {

    private val _exoPlayer = MutableStateFlow<ExoPlayer?>(null)
    val exoPlayer: StateFlow<ExoPlayer?> = _exoPlayer

    private var mediaPlaybackService: MediaPlaybackService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPlaybackService.LocalBinder
            mediaPlaybackService = binder.getService()
            _exoPlayer.value = mediaPlaybackService?.player
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    fun bindService(context: Context) {
        Intent(context, MediaPlaybackService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }

    fun togglePlayPause() {
        _exoPlayer.value?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekTo(position: Long) {
        _exoPlayer.value?.seekTo(position)
    }

    fun seekForward() {
        _exoPlayer.value?.seekForward()
    }

    fun seekBack() {
        _exoPlayer.value?.seekBack()
    }

    fun setPlaybackSpeed(speed: Float) {
        _exoPlayer.value?.setPlaybackSpeed(speed)
    }

    fun changeBrightness(activity: Activity, change: Float) {
        val layoutParams: WindowManager.LayoutParams = activity.window.attributes
        var brightness = layoutParams.screenBrightness
        if (brightness == -1f) {
            brightness = 0.5f
        }
        brightness += change
        layoutParams.screenBrightness = brightness.coerceIn(0f, 1f)
        activity.window.attributes = layoutParams
    }
}
