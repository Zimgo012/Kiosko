package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeoPopButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    showText: Boolean = true,
    enabled: Boolean = true
) {
    Box(modifier = modifier) {
        // Shadow (The "Neo" depth)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(if (enabled) MaterialTheme.colorScheme.secondary else Color.Gray, RoundedCornerShape(12.dp))
        )
        // Main Button
        Surface(
            onClick = { if (enabled) onClick() },
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            color = if (enabled) containerColor else Color.LightGray,
            border = androidx.compose.foundation.BorderStroke(3.dp, if (enabled) MaterialTheme.colorScheme.secondary else Color.Gray)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(iconSize),
                    tint = if (enabled) MaterialTheme.colorScheme.secondary else Color.Gray
                )
                if (showText && text.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (enabled) MaterialTheme.colorScheme.secondary else Color.Gray,
                            letterSpacing = 1.sp,
                            fontSize = fontSize
                        )
                    )
                }
            }
        }
    }
}
