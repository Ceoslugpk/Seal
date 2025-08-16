package com.junkfood.seal.ui.page.player

import android.app.Activity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import org.koin.androidx.compose.koinViewModel
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayer(
    videoPath: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackRate by viewModel.playbackRate.collectAsState()
    val isAudioOnly by viewModel.isAudioOnly.collectAsState()
    val mediaPlayer by viewModel.mediaPlayer.collectAsState()

    LaunchedEffect(videoPath) {
        viewModel.initializePlayer(context, videoPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.video_player)) },
                navigationIcon = {
                    BackButton { onNavigateBack() }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.togglePlayPause() }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (isAudioOnly) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Audio only",
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }
            } else {
                mediaPlayer?.let { player ->
                    AndroidView(
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val (x, y) = dragAmount
                                    val width = size.width
                                    if (change.position.x < width / 2) {
                                        viewModel.changeVolume(if (y > 0) -1 else 1)
                                    } else {
                                        viewModel.changeBrightness(activity, if (y > 0) -0.01f else 0.01f)
                                    }
                                    viewModel.seekTo((x * 10).toLong())
                                }
                            },
                        factory = {
                            VLCVideoLayout(context).apply {
                                player.attachViews(this, null, false, false)
                            }
                        }
                    )
                } ?: run {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            Row {
                Text(text = currentTime.formatDuration())
                Slider(
                    value = currentTime.toFloat(),
                    onValueChange = { viewModel.seek(it.toLong()) },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier.weight(1f)
                )
                Text(text = duration.formatDuration())
            }
            Row {
                Button(onClick = { viewModel.changePlaybackRate() }) {
                    Text(text = "${playbackRate}x")
                }
                Button(onClick = { viewModel.toggleAudioOnly() }) {
                    Icon(
                        imageVector = if (isAudioOnly) Icons.Default.Videocam else Icons.Default.Audiotrack,
                        contentDescription = "Toggle audio only"
                    )
                }
            }
        }
    }
}

private fun Long.formatDuration(): String {
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(this),
        TimeUnit.MILLISECONDS.toSeconds(this) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    )
}
