package com.example.myapplication.ui.booth.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrintPreviewOverlay(
    preview: Bitmap,
    queue: List<Pair<Bitmap, Int>>,
    onQuantityChange: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    isSidePanel: Boolean = false,
    isRendering: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isSidePanel) Modifier else Modifier
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { /* Block touches */ }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isSidePanel) "Checkout" else "Print Strip Builder",
                color = Color.White,
                fontSize = if (isSidePanel) 20.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Queue List
            if (isSidePanel) {
                // Vertical list for side panel to save horizontal space? 
                // Actually LazyRow is still fine if the panel is ~300dp
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    itemsIndexed(queue) { index, (bitmap, quantity) ->
                        QueueItem(
                            bitmap, 
                            quantity, 
                            index, 
                            onQuantityChange, 
                            onRemove, 
                            compact = true,
                            enabled = !isRendering
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    itemsIndexed(queue) { index, (bitmap, quantity) ->
                        QueueItem(
                            bitmap, 
                            quantity, 
                            index, 
                            onQuantityChange, 
                            onRemove, 
                            compact = false,
                            enabled = !isRendering
                        )
                    }
                }
            }
            
            // Dithered Preview Image (Combined)
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .border(1.dp, Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "Combined Dithered Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = if (isRendering) 0.5f else 1f
                )
                
                if (isRendering) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Clear",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                        .clickable(enabled = !isRendering) { onCancel() }
                        .padding(horizontal = if (isSidePanel) 16.dp else 24.dp, vertical = 12.dp)
                )
                Text(
                    text = "Print Strip",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            if (isRendering) Color.Gray else MaterialTheme.colorScheme.primary, 
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !isRendering) { onConfirm() }
                        .padding(horizontal = if (isSidePanel) 16.dp else 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun QueueItem(
    bitmap: Bitmap,
    quantity: Int,
    index: Int,
    onQuantityChange: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    compact: Boolean,
    enabled: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(if (compact) 60.dp else 80.dp)
                    .border(1.dp, Color.White),
                contentScale = ContentScale.Crop,
                alpha = if (enabled) 1f else 0.5f
            )
            // Remove Button
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(if (compact) 16.dp else 20.dp)
                    .background(if (enabled) Color.Red else Color.Gray, CircleShape)
                    .clickable(enabled = enabled) { onRemove(index) }
            )
        }

        // Quantity Control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "-",
                color = Color.White,
                modifier = Modifier
                    .background(if (enabled && quantity > 1) Color.DarkGray else Color.Black.copy(0.2f), CircleShape)
                    .clickable(enabled = enabled && quantity > 1) { onQuantityChange(index, quantity - 1) }
                    .padding(horizontal = if (compact) 6.dp else 8.dp)
            )
            Text(
                text = "$quantity",
                color = if (enabled) Color.White else Color.Gray,
                fontSize = if (compact) 12.sp else 14.sp
            )
            Text(
                text = "+",
                color = Color.White,
                modifier = Modifier
                    .background(if (enabled && quantity < 5) Color.DarkGray else Color.Black.copy(0.2f), CircleShape)
                    .clickable(enabled = enabled && quantity < 5) { onQuantityChange(index, quantity + 1) }
                    .padding(horizontal = if (compact) 6.dp else 8.dp)
            )
        }
    }
}
