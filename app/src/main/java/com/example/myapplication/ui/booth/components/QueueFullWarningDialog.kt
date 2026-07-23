package com.example.myapplication.ui.booth.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun QueueFullWarningDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Print Queue Full") },
        text = { Text("You can only add a maximum of 5 photos to a single print strip.") },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RectangleShape
            ) {
                Text("GOT IT")
            }
        },
        shape = RectangleShape
    )
}
