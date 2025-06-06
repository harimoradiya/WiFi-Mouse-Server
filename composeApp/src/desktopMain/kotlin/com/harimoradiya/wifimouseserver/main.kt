package com.harimoradiya.wifimouseserver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.harimoradiya.wifimouseserver.model.ClientInfo
import com.harimoradiya.wifimouseserver.model.ServerState
import com.harimoradiya.wifimouseserver.server.WifiServer
import com.harimoradiya.wifimouseserver.utils.getLocalIpAddress

import java.awt.Robot


@Composable
fun App() {
    // Permission state management
    var hasPermissions by remember { mutableStateOf(checkAccessibilityPermissions()) }
    var showPermissionDialog by remember { mutableStateOf(!hasPermissions) }
    var initialized by remember { mutableStateOf(false) }
    val server = remember { WifiServer(8080) }
    var serverState by remember { mutableStateOf(ServerState()) }

    // Single initialization block
    LaunchedEffect(Unit) {
        hasPermissions = checkAccessibilityPermissions()
        showPermissionDialog = !hasPermissions
        initialized = true
    }


    LaunchedEffect(server.isRunning.value) {
        serverState = serverState.copy(
            isRunning = server.isRunning.value,
            serverIp = if (server.isRunning.value) getLocalIpAddress() else "0.0.0.0",
            connectedClients = server.connectedClients,
            port = server.currentPort.value
        )
    }

    MaterialTheme {
        if (initialized) {
            if (showPermissionDialog) {
                println("Show ")
                PermissionRequestScreen(
                    onGrantPermission = {
                        openSecurityPreferences()
                    },
                    onDenyPermission = {
                        showPermissionDialog = false
                    }
                )
            } else {
                MainScreen(
                    state = serverState,
                    onStartServer = { server.start() },
                    onStopServer = { server.stop() },
                )
            }
        } else {

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = {
            exitApplication()
        },
        title = "WiFi Mouse Server"
    ) {
        App()
    }
}
// Add these permission functions
fun checkAccessibilityPermissions(): Boolean {
    return try {
        println("Robot can move")
        Robot().mouseMove(0, 0)
        true
    } catch (e: SecurityException) {
        println("Robot can't move ${e.message}")
        false
    }
}


fun openSecurityPreferences() {
    try {
        if (System.getProperty("os.name").contains("mac", ignoreCase = true)) {
            val script = """
                tell application "System Settings"
                    activate
                    delay 1
                    reveal anchor "Privacy_Accessibility" of pane id "com.apple.preference.security"
                end tell
                """.trimIndent()
            Runtime.getRuntime().exec(arrayOf("osascript", "-e", script))
        } else {
            // Windows/Linux alternative
            Robot().mouseMove(0, 0)
        }
    } catch (e: Exception) {
        println("Error opening security preferences: ${e.message}")
    }
}
