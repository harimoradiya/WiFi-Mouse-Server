package com.harimoradiya.wifimouseserver.utils

import com.harimoradiya.wifimouseserver.model.AppInfo
import java.nio.file.Files
import java.nio.file.Paths

object ApplicationLister {
    fun getApplicationList(): List<AppInfo> {
        println("[SERVER] Starting to fetch applications...")
        val apps = mutableListOf<AppInfo>()

        try {
            // Cross-platform application directories
            val applicationDirectories = listOfNotNull(
                getSystemApplicationsDirectory(),
                getUserApplicationsDirectory()
            )

            applicationDirectories.forEach { directory ->
                println("[SERVER] Scanning applications in: $directory")

                // Use Files.walk for more robust directory traversal
                Files.walk(Paths.get(directory), 3)
                    .filter { path ->
                        val file = path.toFile()
                        file.isDirectory && file.extension == "app"
                    }
                    .forEach { path ->
                        val file = path.toFile()
                        try {
                            val icon = IconExtractor.extractAppIcon(file.absolutePath)
                            val bundleId = IconExtractor.getBundleId(file.absolutePath)

                            apps.add(
                                AppInfo(
                                    file.nameWithoutExtension,
                                    file.absolutePath,
                                    icon ?: "",
                                    bundleId ?: ""
                                )
                            )
                            println("[SERVER] Found app: ${file.nameWithoutExtension}")
                        } catch (e: Exception) {
                            println("[SERVER] Error processing app: ${file.name} - ${e.message}")
                        }
                    }
            }
        } catch (e: Exception) {
            println("[SERVER] Error listing applications: ${e.message}")
            e.printStackTrace()
        }

        val sortedApps = apps.sortedBy { it.name }
        println("[SERVER] Total applications found: ${sortedApps.size}")
        return sortedApps
    }

    /**
     * Get system applications directory with cross-platform support
     */
    private fun getSystemApplicationsDirectory(): String? {
        return when {
            System.getProperty("os.name").contains("Mac", ignoreCase = true) -> "/Applications"
            System.getProperty("os.name").contains("Windows", ignoreCase = true) -> "C:\\Program Files"
            System.getProperty("os.name").contains("Linux", ignoreCase = true) -> "/usr/share/applications"
            else -> null
        }
    }

    /**
     * Get user applications directory with cross-platform support
     */
    private fun getUserApplicationsDirectory(): String? {
        return when {
            System.getProperty("os.name").contains("Mac", ignoreCase = true) ->
                "${System.getProperty("user.home")}/Applications"
            System.getProperty("os.name").contains("Windows", ignoreCase = true) ->
                "${System.getProperty("user.home")}\\AppData\\Local\\Programs"
            System.getProperty("os.name").contains("Linux", ignoreCase = true) ->
                "${System.getProperty("user.home")}/.local/share/applications"
            else -> null
        }
    }
}