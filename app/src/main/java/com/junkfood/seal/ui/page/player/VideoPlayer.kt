package com.junkfood.seal.ui.page.player

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.junkfood.seal.MediaPlaybackService
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayer(
    videoPath: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val exoPlayer by viewModel.exoPlayer.collectAsState()

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

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                update = {
                    it.player = exoPlayer
                }
            )
        }
    }
}
