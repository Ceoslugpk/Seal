package com.junkfood.seal.ui.page.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.junkfood.seal.MediaPlaybackService
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.videolan.libvlc.util.VLCVideoLayout
import kotlin.math.abs

@Composable
fun VideoPlayer(
    videoPath: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val player by viewModel.player.collectAsState()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    var gestureConsumed by remember { mutableStateOf(false) }
    var isBrightnessGesture by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000) // Hide controls after 5 seconds
            showControls = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.bindService(context)
        Intent(context, MediaPlaybackService::class.java).also { intent ->
            intent.putExtra("video_path", videoPath)
            context.startService(intent)
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { showControls = !showControls })
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            gestureConsumed = false
                            isBrightnessGesture = offset.x < screenWidthPx / 2
                        },
                        onDrag = { change, dragAmount ->
                            if (!gestureConsumed) {
                                val (x, y) = dragAmount
                                if (abs(y) > abs(x)) { // Vertical Drag
                                    change.consume()
                                    gestureConsumed = true
                                    val delta = -y / screenHeightPx // Invert y, normalize to screen height

                                    if (isBrightnessGesture) {
                                        viewModel.changeBrightness(activity, delta)
                                    } else {
                                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        val newVolume = currentVolume + (maxVolume * delta * 2.0f).toInt()
                                        audioManager.setStreamVolume(
                                            AudioManager.STREAM_MUSIC,
                                            newVolume.coerceIn(0, maxVolume),
                                            0
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            player?.let {
                DisposableEffect(it) {
                    onDispose {
                        it.stop()
                        it.detachViews()
                    }
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { VLCVideoLayout(context) },
                    update = { view ->
                        it.attachViews(view, null, false, false)
                    }
                )

                if (showControls) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        PlayerControls(viewModel = viewModel)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindService(context)
        }
    }
}
