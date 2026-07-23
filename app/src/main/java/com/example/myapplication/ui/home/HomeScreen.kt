package com.example.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.engine.printer.PrintTemplateSettings
import com.example.myapplication.ui.home.settings.FooterSettingsDialog
import com.example.myapplication.ui.home.settings.HeaderSettingsDialog
import com.example.myapplication.ui.theme.BlueGreen
import com.example.myapplication.ui.theme.SkyBlue

@Composable
fun HomeScreen(
    settings: PrintTemplateSettings,
    availableFolders: List<String>,
    onSettingsChange: (PrintTemplateSettings) -> Unit,
    onRefreshFolders: () -> Unit,
    onStartCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTopSettings by remember { mutableStateOf(false) }
    var showBottomSettings by remember { mutableStateOf(false) }

    if (showTopSettings) {
        HeaderSettingsDialog(
            settings = settings,
            onSettingsChange = onSettingsChange,
            onDismiss = { showTopSettings = false }
        )
    }

    if (showBottomSettings) {
        FooterSettingsDialog(
            settings = settings,
            availableFolders = availableFolders,
            onSettingsChange = onSettingsChange,
            onRefreshFolders = onRefreshFolders,
            onDismiss = { showBottomSettings = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {


        // 2. Main Content
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central Camera Graphic
            Box(contentAlignment = Alignment.Center) {
                // White Outer Circle
                Surface(
                    modifier = Modifier.size(200.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = borderStroke(width = 4.dp)
                ) {}
                // Yellow Inner Circle
                Surface(
                    modifier = Modifier.size(150.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary,
                    border = borderStroke(width = 3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }


                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-10).dp, y = (-20).dp)
                        .rotate(10f),
                    shape = RoundedCornerShape(8.dp),
                    color = BlueGreen, // Using BlueGreen from our palette
                    border = borderStroke(width = 2.dp)
                ) {
                    Text(
                        text = "ROLLIE PHOTOBOOTH",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Headline
            Text(
                text = "Ready for your closeup?",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Sub-description
            Text(
                text = "Capture fun memories with high-energy\nfilters and instant prints.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Buttons
            NeoPopButton(
                text = "Start Capturing",
                icon = Icons.Default.CameraAlt,
                containerColor = MaterialTheme.colorScheme.tertiary,
                onClick = onStartCamera,
                modifier = Modifier.fillMaxWidth(0.9f).height(100.dp),
                fontSize = 40.sp,
                iconSize = 48.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            NeoPopButton(
                text = "Settings",
                icon = Icons.Default.Settings,
                containerColor = Color.White,
                onClick = { showTopSettings = true },
                modifier = Modifier.fillMaxWidth(0.9f).height(40.dp),
                fontSize = 14.sp,
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            NeoPopButton(
                text = "Admin Setting",
                icon = Icons.Default.Lock,
                containerColor = Color.White,
                onClick = { showBottomSettings = true },
                modifier = Modifier.fillMaxWidth(0.9f).height(40.dp),
                fontSize = 14.sp,
                iconSize = 20.dp
            )
        }
    }
}

@Composable
fun NeoPopButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box(modifier = modifier) {
        // Shadow (The "Neo" depth)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(6.dp, 6.dp)
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
        )
        // Main Button
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            color = containerColor,
            border = borderStroke(width = 3.dp)
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
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                        fontSize = fontSize
                    )
                )
            }
        }
    }
}

@Composable
private fun borderStroke(width: androidx.compose.ui.unit.Dp = 3.dp) =
    androidx.compose.foundation.BorderStroke(width, MaterialTheme.colorScheme.secondary)
