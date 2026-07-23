package com.example.myapplication.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.engine.printer.PrintTemplateSettings
import com.example.myapplication.ui.home.settings.FooterSettingsDialog
import com.example.myapplication.ui.home.settings.HeaderSettingsDialog

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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rollie Photo Booth",
            style = MaterialTheme.typography.headlineLarge
        )
        
        // 1. Top Template Button
        OutlinedButton(
            onClick = { showTopSettings = true },
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EDIT TOP HEADER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${settings.topText} ${settings.bottomText}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }

        // 2. Bottom Template Button
        OutlinedButton(
            onClick = { showBottomSettings = true },
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EDIT BOTTOM FOOTER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = settings.boothName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Folder: ${settings.clientFolderName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onStartCamera,
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth(0.8f)
        ) {
            Text(
                text = "Start Photo Booth",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
