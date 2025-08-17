package com.junkfood.seal.ui.page.player

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.MediaPlaybackService
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayer(
    audioPath: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackRate by viewModel.playbackRate.collectAsState()
    val audioTracks by viewModel.audioTracks.collectAsState()
    val selectedAudioTrack by viewModel.selectedAudioTrack.collectAsState()

    var showAudioTrackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.bindService(context)
        Intent(context, MediaPlaybackService::class.java).also { intent ->
            intent.putExtra("audio_path", audioPath)
            context.startService(intent)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindService(context)
        }
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
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.audio_player)) },
                navigationIcon = {
                    BackButton { onNavigateBack() }
                }
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = "Audio track",
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
            Column {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.rewind() }) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind"
                        )
                    }
                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                    IconButton(onClick = { viewModel.forward() }) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward"
                        )
                    }
                    Button(onClick = { viewModel.changePlaybackRate() }) {
                        Text(text = "${playbackRate}x")
                    }
                    IconButton(onClick = { showAudioTrackDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Tracks"
                        )
                    }
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
