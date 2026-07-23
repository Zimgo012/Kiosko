package com.example.myapplication.ui.booth.components

import android.graphics.Bitmap
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.controller.booth.FrameType
import com.example.myapplication.ui.components.NeoPopButton
import com.example.myapplication.ui.theme.BlueGreen

@Composable
fun BoothControlsOverlay(
    bitmaps: List<Bitmap>,
    selectedFrame: FrameType,
    isCapturing: Boolean,
    isAutoStartEnabled: Boolean,
    isPrinting: Boolean,
    onToggleAutoStart: () -> Unit,
    onCaptureClick: () -> Unit,
    onRotateCamera: () -> Unit,
    onViewGallery: () -> Unit,
    onPhotoClick: (Bitmap) -> Unit,
    onPrintClick: (Bitmap) -> Unit,
    onFrameTypeSelected: (FrameType) -> Unit,
    onShowRecentPrints: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Gallery Access
        Column(
            modifier = Modifier.weight(1.4f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NeoPopButton(
                text = "Gallery",
                icon = Icons.Default.PhotoLibrary,
                containerColor = Color.White,
                onClick = onViewGallery,
                modifier = Modifier.width(110.dp).height(32.dp),
                fontSize = 10.sp,
                iconSize = 16.dp
            )

            Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                PhotoGallery(
                    bitmaps = bitmaps,
                    onPhotoClick = onPhotoClick,
                    onPrintClick = onPrintClick,
                    isMaximized = false,
                    onToggleMaximize = onViewGallery,
                    enabled = !isPrinting
                )
            }
        }

        // Center: Action Group
        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(4.dp)
                ) {
                    Surface(
                        onClick = { if (!isCapturing) onCaptureClick() },
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

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), CircleShape)
                    .padding(3.dp)
            ) {
                Surface(
                    onClick = onRotateCamera,
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

        // Right: Styles & History
        Column(
            modifier = Modifier.weight(1.3f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedFrame.photoCount > 1) {
                NeoPopButton(
                    text = if (isAutoStartEnabled) "AUTO: ON" else "AUTO: OFF",
                    icon = if (isAutoStartEnabled) Icons.Default.Refresh else Icons.Default.CameraAlt,
                    containerColor = if (isAutoStartEnabled) MaterialTheme.colorScheme.tertiary else Color.White,
                    onClick = onToggleAutoStart,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    fontSize = 11.sp,
                    iconSize = 16.dp,
                    enabled = !isCapturing
                )
            }

            LayoutSelector(
                selectedFrame = selectedFrame,
                isCapturing = isCapturing,
                onFrameTypeSelected = onFrameTypeSelected,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onShowRecentPrints,
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
