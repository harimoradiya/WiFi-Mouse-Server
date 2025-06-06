package com.harimoradiya.wifimouseserver.input

import java.io.File
import java.util.concurrent.TimeUnit

object AppLauncher {
    fun openApplication(appPath: String) {
        val os = System.getProperty("os.name").lowercase()
        val runtime = Runtime.getRuntime()
        val file = File(appPath)

        if (!file.exists()) {
            throw RuntimeException("Application not found at path: $appPath")
        }

        try {
            when {
                os.contains("mac") -> {
                    if (!appPath.endsWith(".app", ignoreCase = true)) {
                        throw RuntimeException("Invalid application path: $appPath. Must be a .app bundle")
                    }
                    val process = runtime.exec(arrayOf("open", appPath))
                    val exitCode = process.waitFor(5, TimeUnit.SECONDS)
                    if (!exitCode) {
                        process.destroyForcibly()
                        throw RuntimeException("Application launch timed out")
                    }
                    val errorStream = process.errorStream.bufferedReader().readText()
                    if (process.exitValue() != 0) {
                        throw RuntimeException("Failed to launch application: $errorStream")
                    }
                    println("[SERVER] Successfully launched application: $appPath")
                }
                os.contains("win") -> runtime.exec(arrayOf("cmd", "/c", "start", "", appPath))
                os.contains("nix") || os.contains("nux") -> runtime.exec(arrayOf("xdg-open", appPath))
                else -> throw RuntimeException("Unsupported operating system: $os")
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is SecurityException -> "Permission denied to launch application"
                is IllegalArgumentException -> "Invalid application path"
                else -> e.message ?: "Unknown error occurred"
            }
            throw RuntimeException("Failed to launch application: $errorMessage")
        }
    }
}
