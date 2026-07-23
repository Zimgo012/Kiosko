package com.example.myapplication.ui.booth.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhotoGallery(
    bitmaps: List<Bitmap>,
    onPhotoClick: (Bitmap) -> Unit,
    onPrintClick: (Bitmap) -> Unit,
    isMaximized: Boolean,
    onToggleMaximize: () -> Unit,
    onAddToPrintQueue: (Bitmap) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (isMaximized) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Full Gallery",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    IconButton(
                        onClick = onToggleMaximize,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    contentPadding = PaddingValues(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(bitmaps) { bitmap ->
                        GalleryItem(
                            bitmap = bitmap,
                            onPhotoClick = onPhotoClick,
                            onPrintClick = onPrintClick,
                            onAddToPrintQueue = onAddToPrintQueue,
                            showAddToQueue = true,
                            enabled = enabled,
                            showPrintButton = true
                        )
                    }
                }
            }
        }
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(bitmaps.take(5)) { bitmap ->
                GalleryItem(
                    bitmap, 
                    onPhotoClick, 
                    onPrintClick, 
                    enabled = enabled,
                    compact = true
                )
            }
        }
    }
}

@Composable
private fun GalleryItem(
    bitmap: Bitmap,
    onPhotoClick: (Bitmap) -> Unit,
    onPrintClick: (Bitmap) -> Unit,
    onAddToPrintQueue: (Bitmap) -> Unit = {},
    showAddToQueue: Boolean = false,
    showPrintButton: Boolean = true,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    val size = if (compact) 115.dp else 180.dp
    
    // Polaroid Style Frame
    Box(
        modifier = Modifier
            .size(width = size, height = size * 1.2f)
            .rotate(if (compact) -2f else 0f) // Slight tilt for flavor
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(if (compact) 6.dp else 10.dp)
            .clickable(enabled = enabled) { onPhotoClick(bitmap) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp)),
                contentScale = ContentScale.Crop
            )
            
            if (!compact) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showPrintButton) {
                        // PRINT Button (Pop out style)
                        Surface(
                            onClick = { onPrintClick(bitmap) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "PRINT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    if (showAddToQueue) {
                        // + QUEUE Button (Pop out style)
                        Surface(
                            onClick = { onAddToPrintQueue(bitmap) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+ QUEUE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
