package com.example.myapplication.ui.booth.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
fun PhotoPreviewOverlay(
    photo: Bitmap,
    onClose: () -> Unit,
    onPrint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .clickable { /* Block touches */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHOTO PREVIEW",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
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

            // Photo Container
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RectangleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Action
            NeoPopButton(
                text = "Print this Photo",
                icon = Icons.Default.Print,
                containerColor = MaterialTheme.colorScheme.tertiary,
                onClick = onPrint,
                modifier = Modifier.fillMaxWidth(0.8f).height(72.dp),
                fontSize = 20.sp,
                iconSize = 28.dp
            )
        }
    }
}
