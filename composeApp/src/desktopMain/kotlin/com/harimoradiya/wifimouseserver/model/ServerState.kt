package com.harimoradiya.wifimouseserver.model

import com.harimoradiya.wifimouseserver.server.WifiServer
import java.time.LocalDateTime


data class ClientInfo(
    val ip: String,
    val hostname: String,
    val connectionTime: LocalDateTime = LocalDateTime.now()
)

data class ServerState(
    val isRunning: Boolean = false,
    val serverIp: String = "0.0.0.0",
    val port: Int = 8080,
    val connectedClients: List<ClientInfo> = emptyList()
)

