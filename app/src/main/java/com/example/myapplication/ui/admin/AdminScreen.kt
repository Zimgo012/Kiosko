package com.example.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.controller.booth.FrameType
import com.example.myapplication.engine.camera.CameraController
import com.example.myapplication.engine.printer.EventNameStyle
import com.example.myapplication.engine.printer.PrintTemplateSettings
import com.example.myapplication.ui.components.NeoPopButton
import com.example.myapplication.viewmodel.admin.AdminViewModel

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    settings: PrintTemplateSettings,
    availableFolders: List<String>,
    isAutoStartEnabled: Boolean,
    selectedFrame: FrameType,
    cameraController: CameraController,
    onSettingsChange: (PrintTemplateSettings) -> Unit,
    onToggleAutoStart: () -> Unit,
    onFrameTypeChange: (FrameType) -> Unit,
    onRefreshFolders: () -> Unit,
    onTestPrint: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()
    val printerStatus by viewModel.printerStatus.collectAsState()
    val cameraStatus by viewModel.cameraStatus.collectAsState()
    val wifiStatus by viewModel.wifiStatus.collectAsState()
    
    var newFolderName by remember { mutableStateOf("") }
    var showCameraPreview by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        onRefreshFolders()
        viewModel.checkDeviceStatus()
    }

    if (showCameraPreview) {
        CameraPreviewDialog(
            cameraController = cameraController,
            onDismiss = { showCameraPreview = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ADMIN PANEL",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.secondary
            )
            
            NeoPopButton(
                text = "Back",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                containerColor = Color.White,
                onClick = onBack,
                modifier = Modifier.width(100.dp).height(48.dp),
                fontSize = 12.sp,
                iconSize = 18.dp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Device Diagnostics Section
        AdminSection(title = "DIAGNOSTICS") {
            StatusItem(
                icon = Icons.Default.Print,
                label = "Printer",
                status = printerStatus,
                action = {
                    NeoPopButton(
                        text = "TEST PRINT",
                        icon = Icons.Default.Print,
                        containerColor = Color.White,
                        onClick = onTestPrint,
                        modifier = Modifier.width(110.dp).height(36.dp),
                        fontSize = 10.sp,
                        iconSize = 14.dp
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatusItem(
                icon = Icons.Default.Devices,
                label = "Camera",
                status = cameraStatus,
                action = {
                    NeoPopButton(
                        text = "PREVIEW",
                        icon = Icons.Default.Visibility,
                        containerColor = Color.White,
                        onClick = { showCameraPreview = true },
                        modifier = Modifier.width(110.dp).height(36.dp),
                        fontSize = 10.sp,
                        iconSize = 14.dp
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatusItem(
                icon = Icons.Default.Wifi,
                label = "WiFi",
                status = wifiStatus
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            NeoPopButton(
                text = "REFRESH DIAGNOSTICS",
                icon = Icons.Default.Devices,
                containerColor = Color.White,
                onClick = { viewModel.checkDeviceStatus() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

//        // Cloud Sync Section
//        AdminSection(title = "CLOUD SYNC") {
//            Text(
//                text = "Automatically sync all captures to your cloud storage account.",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            NeoPopButton(
//                text = if (isCloudSyncEnabled) "DISABLE CLOUD SYNC" else "ENABLE CLOUD SYNC",
//                icon = Icons.Default.CloudSync,
//                containerColor = if (isCloudSyncEnabled) MaterialTheme.colorScheme.primary else Color.White,
//                onClick = { viewModel.toggleCloudSync() },
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                fontSize = 14.sp
//            )
//        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Event Settings Section
        AdminSection(title = "EVENT TEXT & STYLE") {
            // Live Preview Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE HEADER PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (settings.eventStyle == EventNameStyle.NORMAL) {
                        Text(
                            text = "${settings.topText} ${settings.bottomText}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (settings.eventStyle == EventNameStyle.CURSIVE) settings.topText else settings.topText.uppercase(),
                                fontSize = 22.sp,
                                fontWeight = if (settings.eventStyle == EventNameStyle.CURSIVE) FontWeight.Normal else FontWeight.Bold,
                                fontFamily = if (settings.eventStyle == EventNameStyle.CURSIVE) FontFamily.Serif else FontFamily.Default,
                                fontStyle = if (settings.eventStyle == EventNameStyle.CURSIVE) androidx.compose.ui.text.font.FontStyle.Italic else if (settings.eventStyle == EventNameStyle.RETRO) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                color = Color.Black
                            )
                            Text(
                                text = if (settings.eventStyle == EventNameStyle.CURSIVE) settings.bottomText else settings.bottomText.uppercase(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = if (settings.eventStyle == EventNameStyle.MODERN) FontFamily.Default else FontFamily.Serif,
                                fontStyle = if (settings.eventStyle == EventNameStyle.CURSIVE) androidx.compose.ui.text.font.FontStyle.Italic else if (settings.eventStyle == EventNameStyle.RETRO) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = settings.eventDescription,
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                }
            }

            OutlinedTextField(
                value = settings.topText,
                onValueChange = { onSettingsChange(settings.copy(topText = it)) },
                label = { Text("Event Name (Top Line)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.bottomText,
                onValueChange = { onSettingsChange(settings.copy(bottomText = it)) },
                label = { Text("Event Name (Bottom Line)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.eventDescription,
                onValueChange = { onSettingsChange(settings.copy(eventDescription = it)) },
                label = { Text("Event Description") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Event Style", style = MaterialTheme.typography.labelLarge)
            
            Column(Modifier.selectableGroup()) {
                EventNameStyle.entries.forEach { style ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (style == settings.eventStyle),
                                onClick = { onSettingsChange(settings.copy(eventStyle = style)) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (style == settings.eventStyle), onClick = null)
                        Text(text = style.displayName, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Booth Experience Section
        AdminSection(title = "BOOTH EXPERIENCE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Start Mode", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "Automatically start next capture cycle",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Switch(checked = isAutoStartEnabled, onCheckedChange = { onToggleAutoStart() })
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Photo Frame Layout", style = MaterialTheme.typography.labelLarge)
            
            Column(Modifier.selectableGroup()) {
                FrameType.entries.forEach { frame ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (frame == selectedFrame),
                                onClick = { onFrameTypeChange(frame) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (frame == selectedFrame), onClick = null)
                        Text(text = frame.title, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Booth Settings Section
        AdminSection(title = "BOOTH CONTACT INFO") {
            OutlinedTextField(
                value = settings.boothName,
                onValueChange = { onSettingsChange(settings.copy(boothName = it)) },
                label = { Text("Booth Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.phoneNumber,
                onValueChange = { onSettingsChange(settings.copy(phoneNumber = it)) },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.email,
                onValueChange = { onSettingsChange(settings.copy(email = it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Storage Folders Section
        AdminSection(title = "STORAGE FOLDERS") {
            Text(
                text = "Current Folder: ${settings.clientFolderName}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                if (availableFolders.isEmpty()) {
                    Text("No folders found", style = MaterialTheme.typography.bodySmall)
                }
                availableFolders.forEach { folderName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSettingsChange(settings.copy(clientFolderName = folderName)) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Folder, 
                            contentDescription = null,
                            tint = if (settings.clientFolderName == folderName) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (settings.clientFolderName == folderName) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it.replace(Regex("[^a-zA-Z0-9_-]"), "_") },
                    label = { Text("New Folder") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onSettingsChange(settings.copy(clientFolderName = newFolderName))
                            newFolderName = ""
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "v1.0.0 - Admin Build",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun CameraPreviewDialog(
    cameraController: CameraController,
    onDismiss: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).apply {
                            controller = cameraController.controller
                            cameraController.controller.bindToLifecycle(lifecycleOwner)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Close", tint = Color.White, modifier = Modifier.rotate(45f))
                }

                Text(
                    text = "CAMERA PREVIEW",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun StatusItem(
    icon: ImageVector,
    label: String,
    status: String,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
private fun AdminSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
