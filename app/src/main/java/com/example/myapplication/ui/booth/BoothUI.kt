package com.example.myapplication.ui.booth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.ui.booth.components.*
import com.example.myapplication.ui.components.NeoPopButton
import com.example.myapplication.ui.theme.BlueGreen
import com.example.myapplication.viewmodel.booth.BoothViewModel

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
    val showQueueFullWarning by viewModel.showQueueFullWarning.collectAsState()
    
    val flashAlpha = remember { Animatable(0f) }

    LaunchedEffect(currentSessionPhotos.size, bitmaps.size) {
        if (currentSessionPhotos.isNotEmpty() || bitmaps.isNotEmpty()) {
            flashAlpha.snapTo(1f)
            flashAlpha.animateTo(0f, animationSpec = tween(500))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Camera Stage (Full Height)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black,
                    border = borderStroke(width = 4.dp),
                    shadowElevation = 12.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                            onRotateCamera = { cameraController.toggleCamera() },
                            onBack = onBack
                        )

                        // INTERNAL CONTROLS OVERLAY (At the bottom of Camera Screen)
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.25f)) // Lower height feel
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Gallery Access (Widen to cover left side)
                            Column(
                                modifier = Modifier.weight(1.4f), 
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                NeoPopButton(
                                    text = "Gallery",
                                    icon = Icons.Default.PhotoLibrary,
                                    containerColor = Color.White,
                                    onClick = { viewModel.toggleGalleryMaximize() },
                                    modifier = Modifier.width(110.dp).height(32.dp),
                                    fontSize = 10.sp,
                                    iconSize = 16.dp
                                )

                                // Mini Gallery (Taller and Wider)
                                Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                                    PhotoGallery(
                                        bitmaps = bitmaps,
                                        onPhotoClick = { viewModel.setPhotoForPreview(it) },
                                        onPrintClick = { viewModel.prepareForPrint(it) },
                                        isMaximized = false,
                                        onToggleMaximize = { viewModel.toggleGalleryMaximize() },
                                        enabled = !isPrinting
                                    )
                                }
                            }

                            // Center: Action Group (SNAP! is bigger and centered)
                            Row(
                                modifier = Modifier.weight(1.5f), // More weight to center properly
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // SNAP! Button
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp) // Bigger Snap
                                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                            .padding(4.dp)
                                    ) {
                                        Surface(
                                            onClick = {
                                                if (!isCapturing) {
                                                    viewModel.startCaptureCycle { cameraController.takePhoto() }
                                                }
                                            },
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.CameraAlt, 
                                                    contentDescription = "Capture",
                                                    modifier = Modifier.size(44.dp),
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "SNAP!", 
                                        modifier = Modifier.padding(top = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(Modifier.width(16.dp))

                                // Rotate Camera
                                Box(
                                    modifier = Modifier
                                        .size(52.dp) 
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), CircleShape)
                                        .padding(3.dp)
                                ) {
                                    Surface(
                                        onClick = { cameraController.toggleCamera() },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Refresh, 
                                                contentDescription = "Rotate",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }

                            // Right: Wide Styles & History
                            Column(
                                modifier = Modifier.weight(1.3f),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Auto Capture Toggle (Only for multi-photo layouts)
                                if (selectedFrame.photoCount > 1) {
                                    NeoPopButton(
                                        text = if (isAutoStartEnabled) "AUTO: ON" else "AUTO: OFF",
                                        icon = if (isAutoStartEnabled) Icons.Default.Refresh else Icons.Default.CameraAlt,
                                        containerColor = if (isAutoStartEnabled) MaterialTheme.colorScheme.tertiary else Color.White,
                                        onClick = { viewModel.toggleAutoStart() },
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        fontSize = 11.sp,
                                        iconSize = 16.dp,
                                        enabled = !isCapturing
                                    )
                                }

                                LayoutSelector(
                                    selectedFrame = selectedFrame,
                                    isCapturing = isCapturing,
                                    onFrameTypeSelected = { viewModel.setFrameType(it) },
                                    modifier = Modifier.fillMaxWidth() 
                                )

                                Button(
                                    onClick = { viewModel.setShowRecentPrints(true) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueGreen),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("RECENT PRINTS", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- OVERLAYS ---
        if (isGalleryMaximized) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Full Gallery
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

                    // Right Side: Live Strip Preview (So it's not a hassle to add photos)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        if (printingPreview != null) {
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
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Print, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "QUEUE IS EMPTY", 
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        "Tap + QUEUE on any photo", 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

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

        if (printingPreview != null && !isGalleryMaximized) {
            PrintPreviewOverlay(
                preview = printingPreview!!,
                queue = printQueue,
                onQuantityChange = { index, quantity -> viewModel.updateQuantityInQueue(index, quantity) },
                onRemove = { viewModel.removeFromPrintQueue(it) },
                onCancel = { viewModel.cancelPrint() },
                onConfirm = { viewModel.confirmPrint() },
                isRendering = isPrinting
            )
        }

        if (isPrinting && (printingPreview == null)) ProcessingOverlay()

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

        // Queue Full Warning
        if (showQueueFullWarning) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissQueueFullWarning() },
                title = { Text("Print Queue Full") },
                text = { Text("You can only add a maximum of 5 photos to a single print strip.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissQueueFullWarning() },
                        shape = RectangleShape
                    ) {
                        Text("GOT IT")
                    }
                },
                shape = RectangleShape
            )
        }
    }
}

@Composable
private fun borderStroke(width: androidx.compose.ui.unit.Dp = 2.dp) = 
    androidx.compose.foundation.BorderStroke(width, MaterialTheme.colorScheme.secondary)
