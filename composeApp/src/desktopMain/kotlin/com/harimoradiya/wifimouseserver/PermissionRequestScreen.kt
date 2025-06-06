package com.harimoradiya.wifimouseserver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun PermissionRequestScreen(
    onGrantPermission: @Composable () -> Unit,
    onDenyPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDenyPermission,
        title = { Text("Permission Required") },
        text = {
            Column {
                Text("This app needs Accessibility permissions to control your mouse.")
                Spacer(Modifier.height(8.dp))
                Text("1. Click 'Open Settings'")
                Text("2. Check the box next to this app")
                Text("3. Restart the application")
            }
        },
        confirmButton = {
            Button(onClick = {}) {
                Text("Open System Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDenyPermission) {
                Text("Cancel")
            }
        }
    )
}
