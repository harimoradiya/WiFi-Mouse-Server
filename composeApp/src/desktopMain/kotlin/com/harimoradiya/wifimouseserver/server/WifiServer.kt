package com.harimoradiya.wifimouseserver.server

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.harimoradiya.wifimouseserver.model.ClientInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDateTime


class WifiServer(private var port: Int) {
    private var serverSocket: ServerSocket? = null
    private val clients = mutableListOf<Socket>()
    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> get() = _isRunning
    private var scope = CoroutineScope(Dispatchers.IO + Job())
    private val connectionsState = MutableStateFlow(emptyList<String>())
    private val _connectedClients = mutableStateListOf<ClientInfo>()
    val connectedClients: List<ClientInfo> get() = _connectedClients
    private val _currentPort = mutableStateOf(port)
    val currentPort: State<Int> get() = _currentPort


    fun start() {
        if (_isRunning.value) {
            println("[SERVER] Server is already running")
            return
        }

        println("[SERVER] Starting server on port $port...")

        // Create a new scope for the server
        scope = CoroutineScope(Dispatchers.IO + Job())
        scope.launch {
            try {


                serverSocket = ServerSocket(port)
                if (serverSocket?.isBound == true) {
                    _isRunning.value = true
                    println("[SERVER] Started successfully on port $port")
                } else {
                    throw IOException("Failed to bind server socket to port $port")
                }

                while (isRunning.value) {
                    val clientSocket = serverSocket?.accept()
                    clientSocket?.let { socket ->
                        val clientAddress = socket.inetAddress.hostAddress
                        clients.add(socket)
                        connectionsState.update { it + clientAddress }
                        println("[SERVER] New client connected from $clientAddress")
                        launchClientHandler(socket)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when(e) {
                    is java.net.BindException -> "Port $port is already in use"
                    is SecurityException -> "Security manager denied server socket creation"
                    is IOException -> e.message ?: "Failed to create server socket"
                    else -> e.message ?: "Unknown error occurred"
                }
                println("[SERVER] Failed to start: $errorMessage")
                _isRunning.value = false
            }
        }
    }

    private fun launchClientHandler(socket: Socket) {
        scope.launch {
            try {
                val clientIp = socket.inetAddress.hostAddress
                val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val dataInput = DataInputStream(socket.getInputStream())
                val dataOutput = DataOutputStream(socket.getOutputStream())

                // Initialize client info with temporary name
                val clientInfo = ClientInfo(
                    ip = clientIp ?: "Unknown IP",
                    hostname = "Connecting..."
                )
                _connectedClients.add(clientInfo)

                // Set socket timeout for faster disconnect detection
                socket.soTimeout = 30000 // 30 seconds timeout

                while (isRunning.value) {
                    val command = reader.readLine() ?: break
                    val response = InputHandler.processCommand(command, clientIp, dataInput, dataOutput)

                    // Update client info when device name is received
                    if (command.startsWith("DEVICE_NAME:")) {
                        val deviceName = command.substringAfter(":")
                        val index = _connectedClients.indexOfFirst { it.ip == clientIp }
                        if (index != -1) {
                            _connectedClients[index] =
                                _connectedClients[index].copy(hostname = deviceName)
                        }
                    }

                    // Only write response if it's not a file transfer (which handles its own responses)
                    if (!command.startsWith("FILE_TRANSFER_REQUEST")) {
                        writer.write(response)
                        writer.newLine()
                        writer.flush()
                    }
                }
            }
                catch (e: IOException) {
                    println("Client disconnected - ${e.message}")

                } finally {
                    _connectedClients.removeIf { it.ip == socket.inetAddress.hostAddress }
                    socket.close()
                }
            }
        }


    private fun getDeviceName(address: InetAddress): String {
        return try {
            address.canonicalHostName.ifEmpty { "Unknown Device" }
        } catch (e: Exception) {
            address.hostAddress ?: "Unknown Device"
        }
    }


    fun stop() {
        if (!_isRunning.value) {
            println("[SERVER] Server is not running")
            return
        }
        println("[SERVER] Stopping server...")
        _isRunning.value = false
        cleanupResources()
        println("[SERVER] Stopped successfully")
    }

    private fun cleanupResources() {
        println("[SERVER] cleanupResources...")

        // Cancel the coroutine scope first to stop all ongoing operations
        scope.cancel()

        // Notify and close all client connections
        clients.forEach { socket ->
            try {
                if (!socket.isClosed) {
                    val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                    writer.write("SERVER_SHUTDOWN\n")
                    writer.flush()
                    writer.close()
                    socket.close()
                    println("[SERVER] Notified client ${socket.inetAddress.hostAddress} of shutdown")
                }
            } catch (e: Exception) {
                println("[SERVER] Error notifying client ${socket.inetAddress.hostAddress}: ${e.message}")
            }
        }
        clients.clear()
        _connectedClients.clear()


        try {
            serverSocket?.let {
                if (!it.isClosed) {
                    it.close()
                }
            }
            serverSocket = null
        } catch (e: Exception) {
            println("[SERVER] Error closing server socket: ${e.message}")
        }


        connectionsState.update { emptyList() }
    }
}
