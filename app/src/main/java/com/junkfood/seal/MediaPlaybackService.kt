package com.junkfood.seal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MediaPlaybackService : Service() {

    companion object {
        private const val CHANNEL_ID = "playback"
        private const val NOTIF_ID = 1001
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var player: ExoPlayer
    private var started = false

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()

        startForeground(NOTIF_ID, baseNotification("Preparing…"))

        player = ExoPlayer.Builder(this).build().apply {
            setHandleAudioBecomingNoisy(true)
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    // Log error
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("video_path")?.let { uriStr ->
            player.setMediaItem(MediaItem.fromUri(uriStr))
            player.prepare()
            player.playWhenReady = true
        }
        intent?.getStringExtra("audio_path")?.let { uriStr ->
            player.setMediaItem(MediaItem.fromUri(uriStr))
            player.prepare()
            player.playWhenReady = true
        }
        return START_STICKY
    }

    override fun onDestroy() {
        player.release()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = binder

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }

    private fun baseNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_seal)
            .setOngoing(true)
            .build()
}
