package com.harimoradiya.wifimouseserver.server

import com.harimoradiya.wifimouseserver.input.AppLauncher
import com.harimoradiya.wifimouseserver.input.KeyboardController
import com.harimoradiya.wifimouseserver.model.AppInfo
import com.harimoradiya.wifimouseserver.utils.ApplicationLister
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object InputHandler {
    private val clientDevices = mutableMapOf<String, String>()
    private val isMac = System.getProperty("os.name").contains("mac", ignoreCase = true)
    private var fileTransferInProgress = false


    private val robot by lazy {

        try {
            Robot().also {
                println("Robot initialized successfully")
            }
        } catch (e: SecurityException) {
            println("PERMISSION DENIED: ${e.message}")
            null
        }
    }

    suspend fun processCommand(command: String, clientIp: String, input: DataInputStream? = null, output: DataOutputStream? = null): String {
        println("[SERVER] Received command: $command") // <-- ADD THIS

        val parts = command.split(":")
        println("parts - $parts")
        return when (parts[0]) {
            "DEVICE_NAME" -> {
                clientDevices[clientIp] = parts.getOrElse(1) { "Unknown Device" }
                "DEVICE_NAME_SET"
            }
            "MOUSE" -> {
                handleMouseCommand(parts)
                "MOUSE_COMMAND_EXECUTED"
            }
            "KEY" -> {
                handleKeyCommand(parts)
                "KEY_COMMAND_EXECUTED"
            }
            "APP" -> handleAppCommand(parts)
            "SCREENSHOT" -> {
                handleScreenShot(parts)
                "SCREENSHOT_COMMAND_EXECUTED"
            }
            "GESTURE" -> {
                handleGestureCommand(parts)
                "GESTURE_COMMAND_EXECUTED"
            }
            "FILE_TRANSFER_REQUEST" -> {
                if (input == null || output == null) {
                    "ERROR:Invalid file transfer request"
                } else if (fileTransferInProgress) {
                    "ERROR:Another file transfer is in progress"
                } else {
                    try {
                        fileTransferInProgress = true
                        FileTransferHandler.handleFileTransfer(input, output).also {
                            fileTransferInProgress = false
                        }
                    } catch (e: Exception) {
                        fileTransferInProgress = false
                        throw e
                    }
                }
            }
            "GET_DOWNLOAD_DIR" -> {
                "DOWNLOAD_DIR:${FileTransferHandler.getDownloadDirectory()}"
            }
            else -> "UNKNOWN_COMMAND"
        }
    }

    private fun handleScreenShot(parts: List<String> = listOf()) {
        val type = if (parts.size > 1) {
            when(parts[1]) {
                "1" -> "FULL"
                "2" -> "PORTION"
                "3" -> "WINDOW"
                else -> parts[1]
            }
        } else "FULL"
        
        when (type) {
            "FULL" -> {
                if (isMac) {
                    robot?.keyPress(KeyEvent.VK_SHIFT)
                    robot?.keyPress(KeyEvent.VK_META)
                    robot?.keyPress(KeyEvent.VK_3)
                    robot?.keyRelease(KeyEvent.VK_3)
                    robot?.keyRelease(KeyEvent.VK_META)
                    robot?.keyRelease(KeyEvent.VK_SHIFT)
                }
            }
            "PORTION" -> {
                if (isMac) {
                    robot?.keyPress(KeyEvent.VK_SHIFT)
                    robot?.keyPress(KeyEvent.VK_META)
                    robot?.keyPress(KeyEvent.VK_4)
                    robot?.keyRelease(KeyEvent.VK_4)
                    robot?.keyRelease(KeyEvent.VK_META)
                    robot?.keyRelease(KeyEvent.VK_SHIFT)
                }
            }
            "WINDOW" -> {
                if (isMac) {
                    robot?.keyPress(KeyEvent.VK_SHIFT)
                    robot?.keyPress(KeyEvent.VK_META)
                    robot?.keyPress(KeyEvent.VK_4)
                    robot?.keyRelease(KeyEvent.VK_4)
                    robot?.keyPress(KeyEvent.VK_SPACE)
                    robot?.keyRelease(KeyEvent.VK_SPACE)
                    robot?.keyRelease(KeyEvent.VK_META)
                    robot?.keyRelease(KeyEvent.VK_SHIFT)
                }
            }
        }
    }



    private fun handleGestureCommand(parts: List<String>) {
        if (parts.size < 3) return
        
        when (parts[1]) {
            "WORKSPACE" -> {
                when (parts[2]) {
                    "LEFT" -> {
                        if (isMac) {
                            robot?.keyPress(KeyEvent.VK_META)
                            robot?.keyPress(KeyEvent.VK_CONTROL)
                            robot?.keyPress(KeyEvent.VK_LEFT)
                            robot?.keyRelease(KeyEvent.VK_LEFT)
                            robot?.keyRelease(KeyEvent.VK_CONTROL)
                            robot?.keyRelease(KeyEvent.VK_META)
                        }
                    }
                    "RIGHT" -> {
                        if (isMac) {
                            robot?.keyPress(KeyEvent.VK_META)
                            robot?.keyPress(KeyEvent.VK_CONTROL)
                            robot?.keyPress(KeyEvent.VK_RIGHT)
                            robot?.keyRelease(KeyEvent.VK_RIGHT)
                            robot?.keyRelease(KeyEvent.VK_CONTROL)
                            robot?.keyRelease(KeyEvent.VK_META)
                        }
                    }
                }
            }
            "SCROLL" -> {
                if (parts.size < 4) return
                val deltaY = parts[2].toIntOrNull() ?: return
                val velocity = parts[3].toFloatOrNull() ?: 1.0f
                
                // Calculate scroll amount based on velocity
                val scrollAmount = (deltaY * velocity).toInt()
                // Apply smooth scrolling with velocity
                robot?.mouseWheel(scrollAmount * -1)
            }
            "SWIPE" -> {
                if (parts.size < 4) return
                val direction = parts[2]
                val fingerCount = parts[3].toIntOrNull() ?: return
                
                when {
                    fingerCount == 2 -> {
                        when (direction) {
                            "UP" -> robot?.mouseWheel(-3)
                            "DOWN" -> robot?.mouseWheel(3)
                        }
                    }
                    fingerCount == 3 -> {
                        if (isMac) {
                            when (direction) {
                                "LEFT" -> {
                                    robot?.keyPress(KeyEvent.VK_META)
                                    robot?.keyPress(KeyEvent.VK_CONTROL)
                                    robot?.keyPress(KeyEvent.VK_LEFT)
                                    robot?.keyRelease(KeyEvent.VK_LEFT)
                                    robot?.keyRelease(KeyEvent.VK_CONTROL)
                                    robot?.keyRelease(KeyEvent.VK_META)
                                }
                                "RIGHT" -> {
                                    robot?.keyPress(KeyEvent.VK_META)
                                    robot?.keyPress(KeyEvent.VK_CONTROL)
                                    robot?.keyPress(KeyEvent.VK_RIGHT)
                                    robot?.keyRelease(KeyEvent.VK_RIGHT)
                                    robot?.keyRelease(KeyEvent.VK_CONTROL)
                                    robot?.keyRelease(KeyEvent.VK_META)
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    private fun handleMouseCommand(parts: List<String>) {
        when (parts[1]) {
            "MOVE" -> {
                if (parts.size < 4) throw IllegalArgumentException("Invalid MOVE command")
                val dx = parts[2].toInt()
                val dy = parts[3].toInt()
                val currentX = MouseInfo.getPointerInfo().location.x
                val currentY = MouseInfo.getPointerInfo().location.y
                robot?.mouseMove(currentX + dx, currentY + dy)
            }
            "CLICK" -> {
                val button = when (parts[2].toInt()) {
                    1 -> InputEvent.BUTTON1_DOWN_MASK
                    3 -> InputEvent.BUTTON3_DOWN_MASK
                    else -> InputEvent.BUTTON1_DOWN_MASK
                }
                robot?.mousePress(button)
                robot?.mouseRelease(button)
            }
            "SCROLL" -> {
                if (parts.size < 3) {
                    throw IllegalArgumentException("Invalid SCROLL command")
                } else {
                    val scrollAmount = parts[2].toInt()
                    // Negative values scroll up, positive values scroll down
                    // Multiply by -1 to match natural scrolling direction on macOS
                    robot?.mouseWheel(scrollAmount * -1)
                }
            }

        }
    }

    fun getDeviceName(clientIp: String): String {
        return clientDevices[clientIp] ?: clientIp
    }


    private fun handleKeyCommand(parts: List<String>) {
        when (parts[1].uppercase()) {
            "TYPE" -> {
                if (parts.size > 2) KeyboardController.type(parts[2])
            }
            "SPECIAL" -> {
                if (parts.size > 2) {
                    val key = parts[2]
                    val modifiers = if (parts.size > 3) parts.subList(3, parts.size) else emptyList()
                    KeyboardController.pressSpecialKey(key, modifiers)
                }
            }
        }
    }

    private fun handleAppCommand(parts: List<String>): String {
        return when (parts[1].uppercase()) {
            "LIST", "LIST_REQUEST" -> {
                try {
                    val apps = ApplicationLister.getApplicationList()
                    if (apps.isEmpty()) {
                        println("[SERVER] No applications found")
                        "APP_ERROR:No applications found"
                    }
                    else {
                        println("[SERVER] Successfully retrieved ${apps.size} applications")
                        "APP_LIST:${apps.toJsonString()}"
                    }
                } catch (e: Exception) {
                    println("[SERVER] Error listing applications: ${e.message}")
                    e.printStackTrace()
                    "APP_ERROR:Failed to list applications: ${e.message}"
                }
            }
            "LAUNCH" -> {
                if (parts.size < 3) {
                    println("[SERVER] Invalid LAUNCH command format")
                    "APP_LAUNCH_ERROR:Missing application name"
                } else {
                    try {
                        val appName = parts[2]
                        val apps = ApplicationLister.getApplicationList()
                        val app = apps.find { it.name == appName }
                        
                        if (app != null) {
                            println("[SERVER] Found application path: ${app.path}")
                            AppLauncher.openApplication(app.path)
                            "APP_LAUNCH_SUCCESS:${app.name}"
                        } else {
                            println("[SERVER] Application not found: $appName")
                            "APP_LAUNCH_ERROR:Application not found: $appName"
                        }
                    } catch (e: Exception) {
                        println("[SERVER] Error launching application: ${e.message}")
                        e.printStackTrace()
                        "APP_LAUNCH_ERROR:${e.message}"
                    }
                }
            }
            else -> {
                val unknownCommand = parts.getOrNull(1) ?: "<empty>"
                println("[SERVER] Unknown app command: $unknownCommand")
                "APP_ERROR:Unknown command: $unknownCommand"
            }
        }
    }


    private fun List<AppInfo>.toJsonString(): String {
        return this.joinToString(",", "[", "]") { app ->
            "{\"name\":\"${app.name.escapeJson()}\",\"path\":\"${app.path.escapeJson()}\",\"bundleId\":\"${app.bundleId.escapeJson()}\",\"iconBase64\":\"${app.iconBase64.escapeJson()}\"}"
        }
    }

    private fun String.escapeJson(): String {
        return this.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
    }

}