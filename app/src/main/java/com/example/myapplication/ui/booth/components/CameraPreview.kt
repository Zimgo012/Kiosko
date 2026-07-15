package com.example.myapplication.ui.booth.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.engine.camera.CameraController

@Composable
fun CameraPreview(
    cameraController: CameraController,
    lifecycleOwner: LifecycleOwner,
    flashAlpha: Float,
    countdown: Int?,
    isCapturing: Boolean,
    onCaptureClick: () -> Unit,
    onRotateCamera: () -> Unit,
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
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        .padding(40.dp)
                )
            }
        }

        // Overlay Controls (Capture & Rotate)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer to balance the rotate button if needed, but for now just center them
            Box(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(24.dp))

            // Capture Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isCapturing) Color.Gray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f))
                    .border(4.dp, Color.White, CircleShape)
                    .clickable(enabled = !isCapturing) { onCaptureClick() }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.Gray else Color.White)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Camera Rotate Button
            IconButton(
                onClick = onRotateCamera,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotate Camera",
                    tint = Color.White
                )
            }
        }
    }
}
