package com.harimoradiya.wifimouseserver.utils


import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import java.util.Base64
import java.io.IOException

import javax.swing.ImageIcon
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Toolkit
import java.io.FileInputStream


object IconExtractor {



    /**
     * Extract icon from macOS .icns file
     */
    fun extractAppIcon(appPath: String): String? {
        println("[ICON EXTRACTOR] Starting icon extraction for: $appPath")
        val iconPaths = listOf(
            "$appPath/Contents/Resources/app.icns",
            "$appPath/Contents/Resources/icon.icns",
            "$appPath/Contents/Resources/AppIcon.icns",
            "$appPath/Contents/Resources/${File(appPath).nameWithoutExtension}.icns",


            // Additional common patterns
            "$appPath/Contents/Resources/Icon.icns",
            "$appPath/Contents/Resources/Icons/app.icns",
            "$appPath/Contents/Resources/icons/app.icns",


            // App-specific variations
            "$appPath/Contents/Resources/Assets.icns",
            "$appPath/Contents/Resources/Assets/app.icns",

            // Full app name variations
            "$appPath/Contents/Resources/${File(appPath).name}.icns",

            // Lowercase and alternative casing
            "$appPath/Contents/Resources/app.ICNS",
            "$appPath/Contents/Resources/ICON.icns",

            // Nested resource folders
            "$appPath/Contents/Resources/Icons/${File(appPath).nameWithoutExtension}.icns",
            "$appPath/Contents/Resources/icons/${File(appPath).nameWithoutExtension}.icns"

        )

        for (iconPath in iconPaths) {
            val iconFile = File(iconPath)
            if (iconFile.exists()) {
                try {
                    val icon = parseIcnsFile(iconFile)
                    if (icon != null) {
                        println("[ICON EXTRACTOR] Successfully extracted icon from: $iconPath")
                        return imageToBase64(icon)
                    }
                } catch (e: Exception) {
                    println("[ICON EXTRACTOR] Error parsing icon file: ${e.message}")
                }
            }
        }

        // Fallback: Try finding any .icns file
        val icnsFiles = File("$appPath/Contents/Resources")
            .listFiles { file -> file.extension.equals("icns", ignoreCase = true) }

        icnsFiles?.forEach { file ->
            try {
                val icon = parseIcnsFile(file)
                if (icon != null) {
                    println("[ICON EXTRACTOR] Fallback: Found icon in ${file.name}")
                    return imageToBase64(icon)
                }
            } catch (e: Exception) {
                println("[ICON EXTRACTOR] Fallback parsing error: ${e.message}")
            }
        }

        println("[ICON EXTRACTOR] No valid icon found for: $appPath")
        return null
    }

    /**
     * Manual ICNS file parsing
     */
    private fun parseIcnsFile(file: File): BufferedImage? {
        FileInputStream(file).use { input ->
            // Read file header
            val header = ByteArray(8)
            input.read(header)

            // Validate ICNS file signature
            if (!(header[0] == 'i'.toByte() &&
                        header[1] == 'c'.toByte() &&
                        header[2] == 'n'.toByte() &&
                        header[3] == 's'.toByte())) {
                println("[ICON EXTRACTOR] Invalid ICNS file signature")
                return null
            }

            // Track icons found
            val iconData = mutableMapOf<String, ByteArray>()

            // Parse file chunks
            while (input.available() > 0) {
                // Read chunk header
                val typeBytes = ByteArray(4)
                val sizeBytes = ByteArray(4)
                input.read(typeBytes)
                input.read(sizeBytes)

                // Convert bytes to chunk size
                val chunkType = String(typeBytes)
                val chunkSize = bytesToInt(sizeBytes)

                // Read chunk data
                val chunkData = ByteArray(chunkSize - 8)
                input.read(chunkData)

                // Store icon data based on type
                iconData[chunkType] = chunkData
            }

            // Prioritize icon sizes (largest first)
            val preferredSizes = listOf(
                "ic10", // 1024x1024
                "ic09", // 512x512
                "ic08", // 256x256
                "ic07", // 128x128
                "ic04"  // 32x32
            )

            // Try to find the largest icon
            for (size in preferredSizes) {
                val iconBytes = iconData[size]
                if (iconBytes != null) {
                    try {
                        val image = convertToImage(iconBytes)
                        if (image != null) {
                            println("[ICON EXTRACTOR] Found icon type: $size")
                            return resizeImage(image, 128, 128)
                        }
                    } catch (e: Exception) {
                        println("[ICON EXTRACTOR] Error converting icon: ${e.message}")
                    }
                }
            }
        }

        println("[ICON EXTRACTOR] No suitable icon found")
        return null
    }

    /**
     * Convert bytes to integer (big-endian)
     */
    private fun bytesToInt(bytes: ByteArray): Int {
        return ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)
    }

    /**
     * Attempt to convert icon data to BufferedImage
     */
    private fun convertToImage(iconData: ByteArray): BufferedImage? {
        // This is a placeholder. In a real implementation,
        // you'd need to handle different icon compression methods
        try {
            val bais = java.io.ByteArrayInputStream(iconData)
            val image = ImageIO.read(bais)

            if (image != null) {
                println("[ICON EXTRACTOR] Successfully converted icon data")
                return image
            }
        } catch (e: Exception) {
            println("[ICON EXTRACTOR] Image conversion failed: ${e.message}")
        }
        return null
    }

    /**
     * Resize image with high-quality rendering
     */
    private fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val g2 = resizedImage.createGraphics()

        g2.setRenderingHint(
            java.awt.RenderingHints.KEY_INTERPOLATION,
            java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC
        )
        g2.setRenderingHint(
            java.awt.RenderingHints.KEY_RENDERING,
            java.awt.RenderingHints.VALUE_RENDER_QUALITY
        )
        g2.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON
        )

        g2.drawImage(
            originalImage,
            0, 0, targetWidth, targetHeight,
            0, 0, originalImage.width, originalImage.height,
            null
        )
        g2.dispose()

        return resizedImage
    }

    /**
     * Convert image to Base64
     */
    private fun imageToBase64(image: BufferedImage?): String? {
        if (image == null) return null

        try {
            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "png", baos)
            return Base64.getEncoder().encodeToString(baos.toByteArray())
        } catch (e: Exception) {
            println("[ICON EXTRACTOR] Base64 conversion failed: ${e.message}")
            return null
        }
    }


    fun getBundleId(appPath: String): String? {
        println("[BUNDLE ID] Starting bundle ID extraction for: $appPath")

        try {
            val infoPlistPath = "$appPath/Contents/Info.plist"
            val infoPlist = File(infoPlistPath)

            if (!infoPlist.exists()) {
                println("[BUNDLE ID] Info.plist not found at: $infoPlistPath")
                return null
            }

            val content = infoPlist.readText()

            // Comprehensive bundle ID extraction patterns
            val bundleIdPatterns = listOf(
                // XML-style patterns
                "<key>CFBundleIdentifier</key>\\s*<string>(.*?)</string>",

                // Regex for different plist formatting styles
                "CFBundleIdentifier\"[\\s]*=[\\s]*\"(.*?)\"",
                "<string>([a-zA-Z0-9.-]+)</string>\\s*</key>\\s*<key>CFBundleIdentifier",

                // More flexible patterns
                "CFBundleIdentifier\\s*=\\s*\"(.*?)\"",
                "bundleIdentifier\\s*=\\s*\"(.*?)\"",
                "<string>(com\\.[^<]+)</string>"
            )

            for (pattern in bundleIdPatterns) {
                val regex = pattern.toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
                val match = regex.find(content)

                if (match != null) {
                    val extractedBundleId = match.groupValues[1].trim()

                    // Additional validation
                    if (extractedBundleId.isNotBlank() && extractedBundleId.contains('.')) {
                        println("[BUNDLE ID] Successfully extracted Bundle ID: $extractedBundleId")
                        return extractedBundleId
                    }
                }
            }

            println("[BUNDLE ID] No valid bundle identifier found in plist")

            // Fallback: Try to extract from filename or other methods
            val fallbackBundleId = extractFallbackBundleId(appPath)
            if (fallbackBundleId != null) {
                println("[BUNDLE ID] Extracted fallback Bundle ID: $fallbackBundleId")
                return fallbackBundleId
            }

        } catch (e: Exception) {
            println("[BUNDLE ID] Comprehensive error during bundle ID extraction:")
            println("[BUNDLE ID] Error Type: ${e.javaClass.simpleName}")
            println("[BUNDLE ID] Error Message: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    /**
     * Fallback method to extract bundle ID if primary methods fail
     */
    private fun extractFallbackBundleId(appPath: String): String? {
        val appName = File(appPath).nameWithoutExtension

        // Generate a potential bundle ID based on app name
        val generatedBundleId = "com.${appName.lowercase().replace(" ", "")}"

        // Additional checks can be added here
        return generatedBundleId
    }


}
