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
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _playbackRate = MutableStateFlow(1.0f)
    val playbackRate: StateFlow<Float> = _playbackRate
    private val availablePlaybackRates = listOf(1.0f, 1.5f, 2.0f)

    private val _isAudioOnly = MutableStateFlow(false)
    val isAudioOnly: StateFlow<Boolean> = _isAudioOnly

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    private val _subtitleTracks = MutableStateFlow<List<MediaPlayer.TrackDescription>>(emptyList())
    val subtitleTracks: StateFlow<List<MediaPlayer.TrackDescription>> = _subtitleTracks

    private val _selectedSubtitleTrack = MutableStateFlow<MediaPlayer.TrackDescription?>(null)
    val selectedSubtitleTrack: StateFlow<MediaPlayer.TrackDescription?> = _selectedSubtitleTrack

    private val _audioTracks = MutableStateFlow<List<MediaPlayer.TrackDescription>>(emptyList())
    val audioTracks: StateFlow<List<MediaPlayer.TrackDescription>> = _audioTracks

    private val _selectedAudioTrack = MutableStateFlow<MediaPlayer.TrackDescription?>(null)
    val selectedAudioTrack: StateFlow<MediaPlayer.TrackDescription?> = _selectedAudioTrack

    private var mediaPlaybackService: MediaPlaybackService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPlaybackService.LocalBinder
            mediaPlaybackService = binder.getService()
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

    fun selectSubtitleTrack(track: MediaPlayer.TrackDescription) {
        // TODO: call service
    }

    fun selectAudioTrack(track: MediaPlayer.TrackDescription) {
        // TODO: call service
    }

    fun togglePlayPause() {
        // TODO: call service
    }

    fun seek(value: Long) {
        // TODO: call service
    }

    fun seekTo(value: Long) {
        // TODO: call service
    }

    fun forward() {
        seekTo(10000)
    }

    fun rewind() {
        seekTo(-10000)
    }

    fun toggleLock() {
        _isLocked.value = !_isLocked.value
    }

    fun changeVolume(value: Int) {
        // TODO: call service
    }

    fun changePlaybackRate() {
        // TODO: call service
    }

    fun toggleAudioOnly() {
        _isAudioOnly.value = !_isAudioOnly.value
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
