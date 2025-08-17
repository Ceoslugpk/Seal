package com.junkfood.seal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioAttributes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    private var pnManager: PlayerNotificationManager? = null
    private lateinit var mediaSession: MediaSession
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
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(attrs, true) // handles audio focus
            setHandleAudioBecomingNoisy(true)
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    // Log error
                }
            })
        }

        mediaSession = MediaSession.Builder(this, player).build()

        pnManager = PlayerNotificationManager.Builder(this, NOTIF_ID, CHANNEL_ID)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player) =
                    player.mediaMetadata.title?.toString() ?: "Playing"

                override fun createCurrentContentIntent(player: Player): PendingIntent? = null

                override fun getCurrentContentText(player: Player) =
                    player.mediaMetadata.artist?.toString()

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    val artUri = player.mediaMetadata.artworkUri
                    if (artUri != null) {
                        GlobalScope.launch(Dispatchers.IO) {
                            val request = ImageRequest.Builder(applicationContext)
                                .data(artUri)
                                .target {
                                    callback.onBitmap(it.toBitmap())
                                }
                                .build()
                            applicationContext.imageLoader.execute(request)
                        }
                    }
                    return null
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(id: Int, notification: Notification, ongoing: Boolean) {
                    if (ongoing) startForeground(id, notification) else stopForeground(false)
                }
                override fun onNotificationCancelled(id: Int, dismissedByUser: Boolean) {
                    stopForeground(true); stopSelf()
                }
            })
            .build().apply {
                setMediaSessionToken(mediaSession.sessionToken)
                setPlayer(player)
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
        pnManager?.setPlayer(null)
        mediaSession.release()
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
