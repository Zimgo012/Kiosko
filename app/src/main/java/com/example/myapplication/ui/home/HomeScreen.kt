package com.example.myapplication.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.engine.printer.PrintTemplateSettings

@Composable
fun HomeScreen(
    settings: PrintTemplateSettings,
    onSettingsChange: (PrintTemplateSettings) -> Unit,
    onStartCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        PrintSettingsDialog(
            settings = settings,
            onSettingsChange = onSettingsChange,
            onDismiss = { showSettingsDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rollie Photo Booth",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = "Template Settings")
        }
    }
}

@Composable
fun PrintSettingsDialog(
    settings: PrintTemplateSettings,
    onSettingsChange: (PrintTemplateSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Print Template Settings")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = settings.logoText,
                    onValueChange = { onSettingsChange(settings.copy(logoText = it)) },
                    label = { Text("Logo Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = settings.phoneNumber,
                    onValueChange = { onSettingsChange(settings.copy(phoneNumber = it)) },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = settings.email,
                    onValueChange = { onSettingsChange(settings.copy(email = it)) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = settings.message,
                    onValueChange = { onSettingsChange(settings.copy(message = it)) },
                    label = { Text("Bottom Message") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = settings.description,
                    onValueChange = { onSettingsChange(settings.copy(description = it)) },
                    label = { Text("Other Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
