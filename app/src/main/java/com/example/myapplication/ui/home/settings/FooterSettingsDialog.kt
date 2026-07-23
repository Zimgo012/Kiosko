package com.example.myapplication.ui.home.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.engine.printer.PrintTemplateSettings

@Composable
fun FooterSettingsDialog(
    settings: PrintTemplateSettings,
    onSettingsChange: (PrintTemplateSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Footer Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = settings.boothName,
                    onValueChange = { onSettingsChange(settings.copy(boothName = it)) },
                    label = { Text("Booth Name") },
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
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } }
    )
}
