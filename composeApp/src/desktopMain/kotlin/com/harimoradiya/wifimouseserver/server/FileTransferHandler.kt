package com.harimoradiya.wifimouseserver.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

class FileTransferHandler {
    companion object {
        private const val CHUNK_SIZE = 8192 // 8KB chunks for efficient transfer
        private const val DEFAULT_DOWNLOAD_DIR = "Downloads"
        private val downloadDirectory: Path = Paths.get(System.getProperty("user.home"), DEFAULT_DOWNLOAD_DIR)

        init {
            // Ensure download directory exists
            if (!Files.exists(downloadDirectory)) {
                Files.createDirectories(downloadDirectory)
            }
        }

        suspend fun handleFileTransfer(input: DataInputStream, output: DataOutputStream): String = withContext(Dispatchers.IO) {
            try {
                // Read file metadata
                val fileName = input.readUTF()
                val fileSize = input.readLong()
                val expectedChecksum = input.readUTF()

                println("[SERVER] Receiving file: $fileName (${fileSize} bytes)")

                // Create file in downloads directory
                val filePath = downloadDirectory.resolve(fileName)
                val tempFilePath = downloadDirectory.resolve("${fileName}.tmp")

                // Receive file data in chunks
                val digest = MessageDigest.getInstance("SHA-256")
                var bytesReceived = 0L

                FileOutputStream(tempFilePath.toFile()).use { fileOutput ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    while (bytesReceived < fileSize) {
                        val bytesToRead = minOf(CHUNK_SIZE.toLong(), fileSize - bytesReceived).toInt()
                        val bytesRead = input.read(buffer, 0, bytesToRead)
                        if (bytesRead == -1) break

                        fileOutput.write(buffer, 0, bytesRead)
                        digest.update(buffer, 0, bytesRead)
                        bytesReceived += bytesRead

                        // Send progress update
                        val progress = (bytesReceived * 100 / fileSize).toInt()
                        output.writeUTF("PROGRESS:$progress")
                        output.flush()
                    }
                }

                // Verify checksum
                val actualChecksum = digest.digest().joinToString("") { "%02x".format(it) }
                if (actualChecksum != expectedChecksum) {
                    Files.deleteIfExists(tempFilePath)
                    throw IOException("Checksum verification failed")
                }

                // Rename temp file to final name
                Files.move(tempFilePath, filePath)

                "FILE_RECEIVED:$fileName"
            } catch (e: Exception) {
                println("[SERVER] File transfer error: ${e.message}")
                "ERROR:${e.message}"
            }
        }

        fun getDownloadDirectory(): String {
            return downloadDirectory.toString()
        }
    }
}