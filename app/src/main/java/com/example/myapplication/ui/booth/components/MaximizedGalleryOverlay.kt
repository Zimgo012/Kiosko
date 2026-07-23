package com.example.myapplication.ui.booth.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.booth.components.*

@Composable
fun MaximizedGalleryOverlay(
    bitmaps: List<Bitmap>,
    printingPreview: Bitmap?,
    printQueue: List<Pair<Bitmap, Int>>,
    isPrinting: Boolean,
    onPhotoClick: (Bitmap) -> Unit,
    onPrintClick: (Bitmap) -> Unit,
    onAddToPrintQueue: (Bitmap) -> Unit,
    onToggleMaximize: () -> Unit,
    onQuantityChange: (Int, Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onCancelPrint: () -> Unit,
    onConfirmPrint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Side: Full Gallery
            PhotoGallery(
                bitmaps = bitmaps,
                onPhotoClick = onPhotoClick,
                onPrintClick = onPrintClick,
                isMaximized = true,
                onToggleMaximize = onToggleMaximize,
                onAddToPrintQueue = onAddToPrintQueue,
                modifier = Modifier.weight(2f),
                enabled = !isPrinting
            )

            // Right Side: Live Strip Preview
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
                        preview = printingPreview,
                        queue = printQueue,
                        onQuantityChange = onQuantityChange,
                        onRemove = onRemoveFromQueue,
                        onCancel = onCancelPrint,
                        onConfirm = onConfirmPrint,
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
