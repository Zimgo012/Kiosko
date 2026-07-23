package com.example.myapplication.ui.booth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

                        BoothControlsOverlay(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            bitmaps = bitmaps,
                            selectedFrame = selectedFrame,
                            isCapturing = isCapturing,
                            isAutoStartEnabled = isAutoStartEnabled,
                            isPrinting = isPrinting,
                            onToggleAutoStart = { viewModel.toggleAutoStart() },
                            onCaptureClick = {
                                viewModel.startCaptureCycle {
                                    cameraController.takePhoto()
                                }
                            },
                            onRotateCamera = { cameraController.toggleCamera() },
                            onViewGallery = { viewModel.toggleGalleryMaximize() },
                            onPhotoClick = { viewModel.setPhotoForPreview(it) },
                            onPrintClick = { viewModel.prepareForPrint(it) },
                            onFrameTypeSelected = { viewModel.setFrameType(it) },
                            onShowRecentPrints = { viewModel.setShowRecentPrints(true) },
                            onBack = onBack
                        )
                    }
                }
            }
        }

        // --- OVERLAYS ---
        if (isGalleryMaximized) {
            MaximizedGalleryOverlay(
                bitmaps = bitmaps,
                printingPreview = printingPreview,
                printQueue = printQueue,
                isPrinting = isPrinting,
                onPhotoClick = { viewModel.setPhotoForPreview(it) },
                onPrintClick = { viewModel.prepareForPrint(it) },
                onAddToPrintQueue = { viewModel.addToPrintQueue(it) },
                onToggleMaximize = { viewModel.toggleGalleryMaximize() },
                onQuantityChange = { index, quantity -> viewModel.updateQuantityInQueue(index, quantity) },
                onRemoveFromQueue = { viewModel.removeFromPrintQueue(it) },
                onCancelPrint = { viewModel.cancelPrint() },
                onConfirmPrint = { viewModel.confirmPrint() }
            )
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
            QueueFullWarningDialog(
                onDismiss = { viewModel.dismissQueueFullWarning() }
            )
        }
    }
}

@Composable
private fun borderStroke(width: androidx.compose.ui.unit.Dp = 2.dp) = 
    androidx.compose.foundation.BorderStroke(width, MaterialTheme.colorScheme.secondary)
