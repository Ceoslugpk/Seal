package com.junkfood.seal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class MediaPlaybackService : Service() {

    companion object {
        private const val CHANNEL_ID = "playback"
        private const val NOTIF_ID = 1001
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var libVLC: LibVLC
    lateinit var player: MediaPlayer
        private set

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForeground(NOTIF_ID, baseNotification("Preparing…"))

        libVLC = LibVLC(this, ArrayList<String>().apply {
            add("--no-sub-autodetect-file")
            add("--no-spu")
        })
        player = MediaPlayer(libVLC)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val path = intent?.getStringExtra("video_path") ?: intent?.getStringExtra("audio_path")
        if (path != null) {
            val media = Media(libVLC, Uri.parse(path))
            player.media = media
            media.release()
            player.play()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        libVLC.release()
        scope.cancel()
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
