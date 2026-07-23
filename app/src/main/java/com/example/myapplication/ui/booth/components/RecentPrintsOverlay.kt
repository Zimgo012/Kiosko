package com.example.myapplication.ui.booth.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.NeoPopButton

@Composable
fun RecentPrintsOverlay(
    prints: List<Bitmap>,
    onClose: () -> Unit,
    onPrintAgain: (Bitmap) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .clickable { /* Block touches */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRINT HISTORY",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                NeoPopButton(
                    text = "",
                    icon = Icons.Default.Close,
                    containerColor = Color.White,
                    onClick = onClose,
                    modifier = Modifier.size(48.dp),
                    showText = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (prints.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NO RECENT PRINTS FOUND", 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(prints) { print ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Image(
                                    bitmap = print.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                        .background(Color.White),
                                    contentScale = ContentScale.Fit
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                NeoPopButton(
                                    text = "RE-PRINT",
                                    icon = Icons.Default.Print,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    onClick = { onPrintAgain(print) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    fontSize = 12.sp,
                                    showText = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
