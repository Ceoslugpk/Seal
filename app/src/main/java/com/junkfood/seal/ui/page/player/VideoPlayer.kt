package com.junkfood.seal.ui.page.player

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.junkfood.seal.MediaPlaybackService
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import kotlinx.coroutines.delay
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
    val isLocked by viewModel.isLocked.collectAsState()
    val subtitleTracks by viewModel.subtitleTracks.collectAsState()
    val selectedSubtitleTrack by viewModel.selectedSubtitleTrack.collectAsState()
    val audioTracks by viewModel.audioTracks.collectAsState()
    val selectedAudioTrack by viewModel.selectedAudioTrack.collectAsState()

    var controlsVisible by remember { mutableStateOf(true) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioTrackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.bindService(context)
        Intent(context, MediaPlaybackService::class.java).also { intent ->
            intent.putExtra("video_path", videoPath)
            context.startService(intent)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindService(context)
        }
    }

    LaunchedEffect(controlsVisible, isLocked) {
        if (controlsVisible && !isLocked) {
            delay(3000)
            controlsVisible = false
        }
    }

    if (showSubtitleDialog) {
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("Select Subtitle Track") },
            text = {
                LazyColumn {
                    items(subtitleTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectSubtitleTrack(track)
                                    showSubtitleDialog = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = track.id == selectedSubtitleTrack?.id,
                                onClick = {
                                    viewModel.selectSubtitleTrack(track)
                                    showSubtitleDialog = false
                                }
                            )
                            Text(text = track.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAudioTrackDialog) {
        AlertDialog(
            onDismissRequest = { showAudioTrackDialog = false },
            title = { Text("Select Audio Track") },
            text = {
                LazyColumn {
                    items(audioTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectAudioTrack(track)
                                    showAudioTrackDialog = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = track.id == selectedAudioTrack?.id,
                                onClick = {
                                    viewModel.selectAudioTrack(track)
                                    showAudioTrackDialog = false
                                }
                            )
                            Text(text = track.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioTrackDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = controlsVisible && !isLocked,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.video_player)) },
                    navigationIcon = {
                        BackButton { onNavigateBack() }
                    }
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { controlsVisible = !controlsVisible }
                    )
                }
        ) {
            if (isAudioOnly) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Audio only",
                        modifier = Modifier.fillMaxSize(0.5f),
                        tint = Color.White
                    )
                }
            } else {
                // AndroidView for video playback is now managed by the service
                // The UI needs to get the SurfaceView from the service
                // This will be implemented in a later step
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            AnimatedVisibility(
                visible = controlsVisible && !isLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentTime.formatDuration(), color = Color.White)
                        Slider(
                            value = currentTime.toFloat(),
                            onValueChange = { viewModel.seek(it.toLong()) },
                            valueRange = 0f..duration.toFloat(),
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = duration.formatDuration(), color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.rewind() }) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { viewModel.togglePlayPause() }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { viewModel.forward() }) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward",
                                tint = Color.White
                            )
                        }
                        Button(onClick = { viewModel.changePlaybackRate() }) {
                            Text(text = "${playbackRate}x")
                        }
                        Button(onClick = { viewModel.toggleAudioOnly() }) {
                            Icon(
                                imageVector = if (isAudioOnly) Icons.Default.Videocam else Icons.Default.Audiotrack,
                                contentDescription = "Toggle audio only"
                            )
                        }
                        IconButton(onClick = { showSubtitleDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = "Subtitles",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showAudioTrackDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio Tracks",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = { viewModel.toggleLock() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Lock/Unlock",
                    tint = Color.White
                )
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
