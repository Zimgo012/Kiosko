package com.example.myapplication.ui.booth.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.controller.booth.FrameType
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.ui.components.NeoPopButton

@Composable
fun CameraScreen(
    cameraController: CameraController,
    lifecycleOwner: LifecycleOwner,
    flashAlpha: Float,
    countdown: Int?,
    isCapturing: Boolean,
    selectedFrame: FrameType,
    currentSessionPhotosCount: Int,
    isAutoStartEnabled: Boolean,
    onToggleAutoStart: () -> Unit,
    onCaptureClick: () -> Unit,
    onRotateCamera: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
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

        // Overlay: Professional Indicators
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // "LIVE" Indicator Group (Top Left - Pushed down slightly)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp), // Pushed down
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Home Button (Same design as View Gallery)
                NeoPopButton(
                    text = "Return To Home",
                    icon = Icons.Default.Home,
                    containerColor = Color.White,
                    onClick = onBack,
                    modifier = Modifier.width(180.dp).height(36.dp),
                    fontSize = 10.sp,
                    iconSize = 14.dp
                )

                // LIVE Indicator
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
            
            // Resolution/Settings Info (Top Right)
            Text(
                "1080p \u2022 60fps", 
                color = Color.White, 
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Flash Effect Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = flashAlpha }
                .background(Color.White)
        )

        // Countdown Overlay
        countdown?.let { count ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 160.sp,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displayLarge.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
            }
        }
    }
}
