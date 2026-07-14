package com.example.myapplication.engine.camera

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraScreen(
    cameraController: CameraController,
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val bitmaps by viewModel.bitmaps.collectAsState()
    val selectedFrame by viewModel.selectedFrame.collectAsState()
    val currentSessionPhotos by viewModel.currentSessionPhotos.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val isAutoStartEnabled by viewModel.isAutoStartEnabled.collectAsState()
    
    val flashAlpha = remember { Animatable(0f) }

    // Observe session photo count to trigger flash
    LaunchedEffect(currentSessionPhotos.size, bitmaps.size) {
        if (currentSessionPhotos.isNotEmpty() || bitmaps.isNotEmpty()) {
            flashAlpha.snapTo(1f)
            flashAlpha.animateTo(0f, animationSpec = tween(500))
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    controller = cameraController.controller
                    cameraController.controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Flash Effect Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = flashAlpha.value))
        )

        // Camera Rotate Button
        IconButton(
            onClick = { cameraController.toggleCamera() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Rotate Camera",
                tint = Color.White
            )
        }

        // Countdown Overlay
        countdown?.let { count ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        .padding(40.dp)
                )
            }
        }

        // Top Gallery (Recent Photos)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(vertical = 16.dp)
                .height(130.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bitmaps) { bitmap ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "Print",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .clickable { /* TODO: Implement Print */ }
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Auto-start Toggle (Only for grids)
            if (selectedFrame.photoCount > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Auto-next",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = isAutoStartEnabled,
                        onCheckedChange = { viewModel.toggleAutoStart() },
                        enabled = !isCapturing
                    )
                }
            }

            // Photo Progress Indicator
            if (selectedFrame.photoCount > 1) {
                Text(
                    text = if (isCapturing) 
                        "Capturing ${currentSessionPhotos.size + 1} of ${selectedFrame.photoCount}"
                        else "Ready for ${selectedFrame.photoCount} photos",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Frame Type Selector (Disabled while capturing)
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrameType.entries.forEach { frame ->
                    val isSelected = frame == selectedFrame
                    Text(
                        text = frame.title,
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable(enabled = !isCapturing) { viewModel.setFrameType(frame) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Capture Button (Shutter style)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isCapturing) Color.Gray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f))
                    .border(4.dp, Color.White, CircleShape)
                    .clickable(enabled = !isCapturing) {
                        viewModel.startCaptureCycle {
                            cameraController.takePhoto()
                        }
                    }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.Gray else Color.White)
                )
            }
        }
    }
}
