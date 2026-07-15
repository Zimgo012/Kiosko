package com.example.myapplication.ui.booth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.engine.camera.CameraViewModel

@Composable
fun CameraScreen(
    cameraController: CameraController,
    viewModel: CameraViewModel,
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
    
    val flashAlpha = remember { Animatable(0f) }

    // Observe session photo count to trigger flash
    LaunchedEffect(currentSessionPhotos.size, bitmaps.size) {
        if (currentSessionPhotos.isNotEmpty() || bitmaps.isNotEmpty()) {
            flashAlpha.snapTo(1f)
            flashAlpha.animateTo(0f, animationSpec = tween(500))
        }
    }

    BoothUI(
        cameraController = cameraController,
        lifecycleOwner = lifecycleOwner,
        bitmaps = bitmaps,
        selectedFrame = selectedFrame,
        currentSessionPhotosCount = currentSessionPhotos.size,
        countdown = countdown,
        isCapturing = isCapturing,
        isAutoStartEnabled = isAutoStartEnabled,
        isPrinting = isPrinting,
        printingPreview = printingPreview,
        flashAlpha = flashAlpha.value,
        onBack = onBack,
        onToggleAutoStart = { viewModel.toggleAutoStart() },
        onFrameTypeSelected = { viewModel.setFrameType(it) },
        onCaptureClick = {
            viewModel.startCaptureCycle {
                cameraController.takePhoto()
            }
        },
        onRotateCamera = { cameraController.toggleCamera() },
        onPreparePrint = { viewModel.prepareForPrint(it) },
        onCancelPrint = { viewModel.cancelPrint() },
        onConfirmPrint = { viewModel.confirmPrint() },
        modifier = modifier
    )
}
