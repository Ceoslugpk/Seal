package com.junkfood.seal.ui.page.player

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.MediaPlaybackService
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayer(
    audioPath: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val player by viewModel.player.collectAsState()

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
            player?.stop()
        }
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
            PlayerControls(viewModel = viewModel)
        }
    }
}
