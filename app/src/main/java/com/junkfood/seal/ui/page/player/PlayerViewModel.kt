package com.junkfood.seal.ui.page.player

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import com.junkfood.seal.MediaPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.videolan.libvlc.MediaPlayer

class PlayerViewModel() : ViewModel() {

    private val _player = MutableStateFlow<MediaPlayer?>(null)
    val player: StateFlow<MediaPlayer?> = _player

    private var mediaPlaybackService: MediaPlaybackService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPlaybackService.LocalBinder
            mediaPlaybackService = binder.getService()
            _player.value = mediaPlaybackService?.player
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
        _player.value?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekTo(position: Long) {
        _player.value?.time = position
    }

    fun seekForward() {
        _player.value?.let {
            it.time += 10000 // 10 seconds
        }
    }

    fun seekBack() {
        _player.value?.let {
            it.time -= 10000 // 10 seconds
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _player.value?.setRate(speed)
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
