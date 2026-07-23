package com.example.myapplication.ui.home.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.engine.printer.EventNameStyle
import com.example.myapplication.engine.printer.PrintTemplateSettings

@Composable
fun HeaderSettingsDialog(
    settings: PrintTemplateSettings,
    onSettingsChange: (PrintTemplateSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Header Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stylish Preview Card inside the dialog
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LIVE HEADER PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // New Preview Logic for different styles
                        when(settings.eventStyle) {
                            EventNameStyle.RETRO -> {
                                val words = settings.eventName.split(" ")
                                val first = words.getOrNull(0) ?: ""
                                val rest = if (words.size > 1) words.drop(1).joinToString(" ") else ""
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = first.uppercase(),
                                        fontSize = 22.sp, // Bigger
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, // Slanted
                                        color = Color.Black
                                    )
                                    Text(
                                        text = rest.uppercase(),
                                        fontSize = 32.sp, // Bigger
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, // Slanted
                                        color = Color.Black
                                    )
                                }
                            }
                            EventNameStyle.CURSIVE -> {
                                Text(
                                    text = settings.eventName,
                                    fontSize = 28.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color.Black
                                )
                            }
                            EventNameStyle.MODERN -> {
                                Text(
                                    text = settings.eventName.uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                            EventNameStyle.NORMAL -> {
                                Text(
                                    text = settings.eventName,
                                    fontSize = 24.sp,
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = settings.eventName,
                    onValueChange = { onSettingsChange(settings.copy(eventName = it)) },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = settings.eventDescription,
                    onValueChange = { onSettingsChange(settings.copy(eventDescription = it)) },
                    label = { Text("Event Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Style Selection", style = MaterialTheme.typography.titleSmall)

                Column(Modifier.selectableGroup()) {
                    EventNameStyle.entries.forEach { style ->
                        Row(
                            Modifier.fillMaxWidth().height(48.dp).selectable(
                                selected = (style == settings.eventStyle),
                                onClick = { onSettingsChange(settings.copy(eventStyle = style)) },
                                role = Role.RadioButton
                            ).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (style == settings.eventStyle), onClick = null)
                            Text(text = style.displayName, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } }
    )
}
