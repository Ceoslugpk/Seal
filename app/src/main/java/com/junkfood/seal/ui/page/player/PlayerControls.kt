package com.junkfood.seal.ui.page.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.util.concurrent.TimeUnit

@Composable
fun PlayerControls(
    viewModel: PlayerViewModel
) {
    val player by viewModel.player.collectAsState()
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    val playbackSpeeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)

    player?.let {
        Column {
            val duration = it.length
            val position = it.time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = position.formatDuration())
                Slider(
                    value = position.toFloat(),
                    onValueChange = { newPosition -> viewModel.seekTo(newPosition.toLong()) },
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
                IconButton(onClick = { viewModel.seekBack() }) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind"
                    )
                }
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        imageVector = if (it.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }
                IconButton(onClick = { viewModel.seekForward() }) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward"
                    )
                }
                IconButton(onClick = {
                    val currentIndex = playbackSpeeds.indexOf(playbackSpeed)
                    val nextIndex = (currentIndex + 1) % playbackSpeeds.size
                    playbackSpeed = playbackSpeeds[nextIndex]
                    viewModel.setPlaybackSpeed(playbackSpeed)
                }) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Playback Speed"
                    )
                }
                Text(text = "${playbackSpeed}x")
            }
        }
    }
}

private fun Long.formatDuration(): String {
    if (this < 0) return "00:00"
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(this),
        TimeUnit.MILLISECONDS.toSeconds(this) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    )
}
