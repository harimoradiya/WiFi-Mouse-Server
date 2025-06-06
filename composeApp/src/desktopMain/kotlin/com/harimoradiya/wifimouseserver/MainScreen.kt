package com.harimoradiya.wifimouseserver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.harimoradiya.wifimouseserver.model.ServerState
import com.harimoradiya.wifimouseserver.server.InputHandler

@Composable
fun MainScreen(
    state: ServerState,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Server Status Card
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (state.isRunning) Icons.Filled.CheckCircle else Icons.Filled.Close,
                        contentDescription = "Server Status",
                        tint = if (state.isRunning) Color(0xFF4CAF50) else Color(0xFFE57373),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (state.isRunning) "Server Running" else "Server Stopped",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (state.isRunning) Color(0xFF4CAF50) else Color(0xFFE57373)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Control Buttons
               ElevatedButton(
                    onClick = { if (state.isRunning) onStopServer() else onStartServer() },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (state.isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onError.takeIf { state.isRunning } ?: MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp,
                        hoveredElevation = 8.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (state.isRunning) Icons.Filled.Close else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isRunning) "Stop Server" else "Start Server",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (state.isRunning) "Stop Server" else "Start Server",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (!state.isRunning) {
                    Text(
                        text = "⚠️ Accessibility permissions required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Server Information
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Server Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("IP: ${state.serverIp}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(16.dp))
                        Text("Port: ${state.port}", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Connected Devices Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Connected Devices (${state.connectedClients.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.connectedClients) { client ->
                            val deviceName = InputHandler.getDeviceName(client.ip)
                            androidx.compose.material3.Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                elevation = androidx.compose.material3.CardDefaults.cardElevation(
                                    defaultElevation = 2.dp,
                                    hoveredElevation = 4.dp,
                                    pressedElevation = 1.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = deviceName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = client.ip,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Connected at ${client.connectionTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }


                Spacer(Modifier.height(24.dp))



            }
        }
    }
}
