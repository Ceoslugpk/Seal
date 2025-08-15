package com.junkfood.seal.ui.page.player

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.File

@KoinViewModel
class PlayerViewModel : ViewModel() {
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


    private lateinit var libVLC: LibVLC
    lateinit var mediaPlayer: MediaPlayer

    fun initializePlayer(context: Context, videoPath: String) {
        viewModelScope.launch {
            libVLC = LibVLC(context, ArrayList<String>().apply { add("--no-stats") })
            mediaPlayer = MediaPlayer(libVLC)
            val media = Media(libVLC, Uri.fromFile(File(videoPath)))
            mediaPlayer.media = media
            media.release()
            mediaPlayer.play()
            _duration.value = mediaPlayer.length
            viewModelScope.launch {
                while (true) {
                    _currentTime.value = mediaPlayer.time
                    delay(100)
                }
            }
        }
    }

    fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _isPlaying.value = false
        } else {
            mediaPlayer.play()
            _isPlaying.value = true
        }
    }

    fun seek(value: Long) {
        mediaPlayer.time = value
    }

    fun seekTo(value: Long) {
        val newTime = mediaPlayer.time + value
        mediaPlayer.time = newTime.coerceIn(0, mediaPlayer.length)
    }

    fun changeVolume(value: Int) {
        val newVolume = mediaPlayer.volume + value
        mediaPlayer.volume = newVolume.coerceIn(0, 100)
    }

    fun changePlaybackRate() {
        val currentIndex = availablePlaybackRates.indexOf(_playbackRate.value)
        val nextIndex = (currentIndex + 1) % availablePlaybackRates.size
        _playbackRate.value = availablePlaybackRates[nextIndex]
        mediaPlayer.rate = _playbackRate.value
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

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.stop()
        mediaPlayer.release()
        libVLC.release()
    }
}
