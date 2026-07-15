package com.example.myapplication.ui.booth

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.engine.camera.FrameType
import com.example.myapplication.ui.booth.components.*

@Composable
fun BoothUI(
    cameraController: CameraController,
    lifecycleOwner: LifecycleOwner,
    bitmaps: List<Bitmap>,
    selectedFrame: FrameType,
    currentSessionPhotosCount: Int,
    countdown: Int?,
    isCapturing: Boolean,
    isAutoStartEnabled: Boolean,
    isPrinting: Boolean,
    printingPreview: Bitmap?,
    flashAlpha: Float,
    onBack: () -> Unit,
    onToggleAutoStart: () -> Unit,
    onFrameTypeSelected: (FrameType) -> Unit,
    onCaptureClick: () -> Unit,
    onRotateCamera: () -> Unit,
    onPreparePrint: (Bitmap) -> Unit,
    onCancelPrint: () -> Unit,
    onConfirmPrint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        var isGalleryMaximized by remember { mutableStateOf(false) }

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Side: Camera Preview
            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxSize()
            ) {
                // Camera Container with Border
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize()
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(2.dp) // Internal padding to prevent overlap
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    CameraPreview(
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        flashAlpha = flashAlpha,
                        countdown = countdown,
                        isCapturing = isCapturing,
                        onCaptureClick = onCaptureClick,
                        onRotateCamera = onRotateCamera
                    )
                }
            }

            // Right Side: Controls and Gallery
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(24.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    .padding(2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Row for Back Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = Color.White
                            )
                        }
                    }

                    // Controls Area (Middle)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Buttons(
                            selectedFrame = selectedFrame,
                            isCapturing = isCapturing,
                            isAutoStartEnabled = isAutoStartEnabled,
                            currentSessionPhotosCount = currentSessionPhotosCount,
                            onToggleAutoStart = onToggleAutoStart,
                            onFrameTypeSelected = onFrameTypeSelected
                        )
                    }

                    // Gallery Area (Bottom)
                    PhotoGallery(
                        bitmaps = bitmaps,
                        onPrintClick = onPreparePrint,
                        isMaximized = false,
                        onToggleMaximize = { isGalleryMaximized = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Maximized Gallery Overlay
        if (isGalleryMaximized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                PhotoGallery(
                    bitmaps = bitmaps,
                    onPrintClick = onPreparePrint,
                    isMaximized = true,
                    onToggleMaximize = { isGalleryMaximized = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Print Preview Dialog / Overlay
        printingPreview?.let { preview ->
            PrintPreviewOverlay(
                preview = preview,
                onCancel = onCancelPrint,
                onConfirm = onConfirmPrint
            )
        }

        // Processing / Printing Progress Overlay
        if (isPrinting && printingPreview == null) {
            ProcessingOverlay()
        }
    }
}
