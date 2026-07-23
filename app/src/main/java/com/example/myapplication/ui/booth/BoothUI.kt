package com.example.myapplication.ui.booth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.viewmodel.booth.BoothViewModel
import com.example.myapplication.ui.booth.components.*

@Composable
fun BoothUI(
    cameraController: CameraController,
    viewModel: BoothViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val bitmaps by viewModel.bitmaps.collectAsState()
    val selectedFrame by viewModel.selectedFrame.collectAsState()
    val currentSessionPhotos by viewModel.currentSessionPhotos.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val isAutoStartEnabled by viewModel.isAutoStartEnabled.collectAsState()
    val isPrinting by viewModel.isPrinting.collectAsState()
    val printingPreview by viewModel.printingPreview.collectAsState()
    val printQueue by viewModel.printQueue.collectAsState()
    val isGalleryMaximized by viewModel.isGalleryMaximized.collectAsState()
    val selectedPhotoForPreview by viewModel.selectedPhotoForPreview.collectAsState()
    val recentPrints by viewModel.recentPrints.collectAsState()
    val showRecentPrints by viewModel.showRecentPrints.collectAsState()
    
    val flashAlpha = remember { Animatable(0f) }

    // Observe session photo count to trigger flash
    LaunchedEffect(currentSessionPhotos.size, bitmaps.size) {
        if (currentSessionPhotos.isNotEmpty() || bitmaps.isNotEmpty()) {
            flashAlpha.snapTo(1f)
            flashAlpha.animateTo(0f, animationSpec = tween(500))
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
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
                    CameraScreen(
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        flashAlpha = flashAlpha.value,
                        countdown = countdown,
                        isCapturing = isCapturing,
                        selectedFrame = selectedFrame,
                        currentSessionPhotosCount = currentSessionPhotos.size,
                        isAutoStartEnabled = isAutoStartEnabled,
                        onToggleAutoStart = { viewModel.toggleAutoStart() },
                        onCaptureClick = {
                            viewModel.startCaptureCycle {
                                cameraController.takePhoto()
                            }
                        },
                        onRotateCamera = { cameraController.toggleCamera() }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.setShowRecentPrints(true) },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Recent Prints",
                                tint = Color.White
                            )
                        }

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
                            onFrameTypeSelected = { viewModel.setFrameType(it) }
                        )
                    }

                    // Gallery Area (Bottom)
                    PhotoGallery(
                        bitmaps = bitmaps,
                        onPhotoClick = { viewModel.setPhotoForPreview(it) },
                        onPrintClick = { viewModel.prepareForPrint(it) },
                        isMaximized = false,
                        onToggleMaximize = { viewModel.toggleGalleryMaximize() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPrinting
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
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Gallery (2/3)
                    PhotoGallery(
                        bitmaps = bitmaps,
                        onPhotoClick = { viewModel.setPhotoForPreview(it) },
                        onPrintClick = { viewModel.prepareForPrint(it) },
                        isMaximized = true,
                        onToggleMaximize = { viewModel.toggleGalleryMaximize() },
                        onAddToPrintQueue = { viewModel.addToPrintQueue(it) },
                        modifier = Modifier.weight(2f),
                        enabled = !isPrinting
                    )

                    // Right Side: Checkout Panel (1/3)
                    if (printingPreview != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(Color.DarkGray.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            PrintPreviewOverlay(
                                preview = printingPreview!!,
                                queue = printQueue,
                                onQuantityChange = { index, quantity -> viewModel.updateQuantityInQueue(index, quantity) },
                                onRemove = { viewModel.removeFromPrintQueue(it) },
                                onCancel = { viewModel.cancelPrint() },
                                onConfirm = { viewModel.confirmPrint() },
                                isSidePanel = true,
                                isRendering = isPrinting
                            )
                        }
                    } else {
                        // Empty state for checkout when nothing is selected
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(Color.DarkGray.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select photos to print",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        // Full Photo Preview Overlay
        selectedPhotoForPreview?.let { photo ->
            PhotoPreviewOverlay(
                photo = photo,
                onClose = { viewModel.setPhotoForPreview(null) },
                onPrint = {
                    viewModel.setPhotoForPreview(null)
                    viewModel.prepareForPrint(photo)
                }
            )
        }

        // Print Preview Dialog / Overlay (only if gallery is NOT maximized)
        if (!isGalleryMaximized) {
            printingPreview?.let { preview ->
                PrintPreviewOverlay(
                    preview = preview,
                    queue = printQueue,
                    onQuantityChange = { index, quantity -> viewModel.updateQuantityInQueue(index, quantity) },
                    onRemove = { viewModel.removeFromPrintQueue(it) },
                    onCancel = { viewModel.cancelPrint() },
                    onConfirm = { viewModel.confirmPrint() },
                    isRendering = isPrinting
                )
            }
        }

        // Processing / Printing Progress Overlay
        if (isPrinting && (printingPreview == null)) {
            ProcessingOverlay()
        }

        // Recent Prints History Overlay
        if (showRecentPrints) {
            RecentPrintsOverlay(
                prints = recentPrints,
                onClose = { viewModel.setShowRecentPrints(false) },
                onPrintAgain = { bitmap ->
                    viewModel.prepareForPrint(bitmap) 
                    viewModel.setShowRecentPrints(false)
                }
            )
        }
    }
}
