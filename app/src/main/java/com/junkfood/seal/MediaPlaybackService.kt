package com.junkfood.seal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class MediaPlaybackService : Service() {

    lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusChangeListener: AudioFocusChangeListener

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(MediaSessionCallback())
            .build()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusChangeListener = AudioFocusChangeListener(exoPlayer)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                val videoPath = intent?.getStringExtra("video_path")
                val audioPath = intent?.getStringExtra("audio_path")
                val path = videoPath ?: audioPath
                path?.let {
                    val mediaItem = MediaItem.fromUri(Uri.fromFile(File(it)))
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                    requestAudioFocus()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        startForeground(1, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        mediaSession.release()
        abandonAudioFocus()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media_playback_channel",
                "Media Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = NotificationCompat.Builder(this, "media_playback_channel")
            .setSmallIcon(R.drawable.ic_stat_seal)
            .setContentTitle("Now Playing")
            .setContentText("...")
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return builder.build()
    }

    fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onPlayerCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int,
            extras: android.os.Bundle
        ): ListenableFuture<MediaSession.ConnectionResult> {
            if (playerCommand == Player.COMMAND_PLAY_PAUSE) {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }
            return super.onPlayerCommand(session, controller, playerCommand, extras)
        }
    }
}
