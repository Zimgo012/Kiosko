package com.example.myapplication.ui.booth.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.NeoPopButton

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
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                    .clickable { /* Block touches */ }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).fillMaxSize()
        ) {
            Text(
                text = if (isSidePanel) "CHECKOUT" else "STRIP BUILDER",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = if (isSidePanel) 24.sp else 32.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Queue List
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(queue) { index, (bitmap, quantity) ->
                    QueueItem(
                        bitmap, 
                        quantity, 
                        index, 
                        onQuantityChange, 
                        onRemove, 
                        compact = isSidePanel,
                        enabled = !isRendering
                    )
                }
            }
            
            // Dithered Preview Image (Combined)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RectangleShape,
                color = Color(0xFF263238), // Dark Slate for contrast against white paper
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = "Combined Preview",
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit,
                        alpha = if (isRendering) 0.5f else 1f
                    )
                    
                    if (isRendering) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NeoPopButton(
                    text = "Clear",
                    icon = Icons.Default.Delete,
                    containerColor = Color.White,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(60.dp),
                    enabled = !isRendering
                )
                NeoPopButton(
                    text = "Print",
                    icon = Icons.Default.Print,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1.5f).height(60.dp),
                    enabled = !isRendering
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
            Surface(
                modifier = Modifier.size(if (compact) 70.dp else 100.dp),
                shape = RectangleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Crop,
                    alpha = if (enabled) 1f else 0.5f
                )
            }
            // Remove Button
            IconButton(
                onClick = { onRemove(index) },
                enabled = enabled,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(24.dp)
                    .background(if (enabled) MaterialTheme.colorScheme.error else Color.Gray, RectangleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quantity Control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RectangleShape).padding(horizontal = 4.dp)
        ) {
            Text(
                text = "-",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = enabled && quantity > 1) { onQuantityChange(index, quantity - 1) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = "$quantity",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "+",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = enabled && quantity < 5) { onQuantityChange(index, quantity + 1) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
