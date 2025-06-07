package com.harimoradiya.wifimouseserver.utils

import com.harimoradiya.wifimouseserver.model.AppInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object ApplicationLister {
    fun getApplicationList(): List<AppInfo> {
        println("[SERVER] Starting to fetch applications...")
        val apps = mutableListOf<AppInfo>()

        try {
            when {
                System.getProperty("os.name").contains("Mac", ignoreCase = true) -> {
                    getMacApplications(apps)
                }
                System.getProperty("os.name").contains("Windows", ignoreCase = true) -> {
                    getWindowsApplications(apps)
                }
                System.getProperty("os.name").contains("Linux", ignoreCase = true) -> {
                    getLinuxApplications(apps)
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

    private fun getMacApplications(apps: MutableList<AppInfo>) {
        val applicationDirectories = listOf(
            "/Applications",
            "${System.getProperty("user.home")}/Applications"
        )

        applicationDirectories.forEach { directory ->
            println("[SERVER] Scanning Mac applications in: $directory")

            Files.walk(Paths.get(directory), 2)
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
                        println("[SERVER] Found Mac app: ${file.nameWithoutExtension}")
                    } catch (e: Exception) {
                        println("[SERVER] Error processing Mac app: ${file.name} - ${e.message}")
                    }
                }
        }
    }

    private fun getWindowsApplications(apps: MutableList<AppInfo>) {
        // Method 1: Get from Start Menu shortcuts (most reliable)
        getStartMenuApplications(apps)

        // Method 2: Scan specific known application directories
        scanKnownApplicationDirectories(apps)

        // Method 3: Get from Windows Registry
        getWindowsRegistryApplications(apps)
    }

    private fun getStartMenuApplications(apps: MutableList<AppInfo>) {
        val startMenuPaths = listOf(
            "C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs",
            "${System.getProperty("user.home")}\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs"
        )

        startMenuPaths.forEach { path ->
            val startMenuDir = File(path)
            if (startMenuDir.exists()) {
                println("[SERVER] Scanning Start Menu: $path")
                scanStartMenuDirectory(startMenuDir, apps)
            }
        }
    }

    private fun scanStartMenuDirectory(directory: File, apps: MutableList<AppInfo>) {
        try {
            directory.walkTopDown().forEach { file ->
                if (file.isFile && file.extension.lowercase() == "lnk") {
                    try {
                        val appName = file.nameWithoutExtension
                        // Filter out system/uninstall shortcuts
                        if (isValidUserApplication(appName)) {
                            apps.add(
                                AppInfo(
                                    appName,
                                    file.absolutePath,
                                    "",
                                    ""
                                )
                            )
                            println("[SERVER] Found Start Menu app: $appName")
                        }
                    } catch (e: Exception) {
                        println("[SERVER] Error processing shortcut: ${file.name} - ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("[SERVER] Error scanning Start Menu: ${directory.absolutePath} - ${e.message}")
        }
    }

    private fun scanKnownApplicationDirectories(apps: MutableList<AppInfo>) {
        // Known directories where major applications install their main executables
        val knownAppPaths = mapOf(
            "Google Chrome" to listOf(
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
            ),
            "Mozilla Firefox" to listOf(
                "C:\\Program Files\\Mozilla Firefox\\firefox.exe",
                "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe"
            ),
            "Brave Browser" to listOf(
                "${System.getProperty("user.home")}\\AppData\\Local\\BraveSoftware\\Brave-Browser\\Application\\brave.exe"
            ),
            "Microsoft Edge" to listOf(
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe"
            ),
            "VLC Media Player" to listOf(
                "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
                "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"
            ),
            "Visual Studio Code" to listOf(
                "${System.getProperty("user.home")}\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe"
            ),
            "Notepad++" to listOf(
                "C:\\Program Files\\Notepad++\\notepad++.exe",
                "C:\\Program Files (x86)\\Notepad++\\notepad++.exe"
            ),
            "7-Zip" to listOf(
                "C:\\Program Files\\7-Zip\\7zFM.exe",
                "C:\\Program Files (x86)\\7-Zip\\7zFM.exe"
            ),
            "WinRAR" to listOf(
                "C:\\Program Files\\WinRAR\\WinRAR.exe",
                "C:\\Program Files (x86)\\WinRAR\\WinRAR.exe"
            ),
            "Adobe Acrobat Reader" to listOf(
                "C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe",
                "C:\\Program Files (x86)\\Adobe\\Acrobat Reader DC\\Reader\\AcroRd32.exe"
            ),
            "Discord" to listOf(
                "${System.getProperty("user.home")}\\AppData\\Local\\Discord\\Update.exe"
            ),
            "Spotify" to listOf(
                "${System.getProperty("user.home")}\\AppData\\Roaming\\Spotify\\Spotify.exe"
            ),
            "Android Studio" to listOf(
                "${System.getProperty("user.home")}\\AppData\\Local\\Google\\AndroidStudio\\bin\\studio64.exe",
                "C:\\Program Files\\Android\\Android Studio\\bin\\studio64.exe"
            )
        )

        knownAppPaths.forEach { (appName, paths) ->
            paths.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    apps.add(
                        AppInfo(
                            appName,
                            path,
                            "",
                            ""
                        )
                    )
                    println("[SERVER] Found known app: $appName at $path")
                }
            }
        }
    }

    private fun getWindowsRegistryApplications(apps: MutableList<AppInfo>) {
        try {
            // Read from Windows Registry for installed programs
            // This requires executing registry commands
            val process = ProcessBuilder(
                "reg",
                "query",
                "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                "/s"
            ).start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            // Parse registry output to extract application information
            parseRegistryOutput(output, apps)
        } catch (e: Exception) {
            println("[SERVER] Error reading Windows registry: ${e.message}")
        }
    }

    private fun parseRegistryOutput(output: String, apps: MutableList<AppInfo>) {
        val entries = output.split("\r\n\r\n")

        entries.forEach { entry ->
            try {
                val lines = entry.split("\r\n")
                var displayName: String? = null
                var installLocation: String? = null
                var displayIcon: String? = null
                var uninstallString: String? = null

                lines.forEach { line ->
                    when {
                        line.contains("DisplayName") && line.contains("REG_SZ") -> {
                            displayName = line.substringAfter("REG_SZ").trim()
                        }
                        line.contains("InstallLocation") && line.contains("REG_SZ") -> {
                            installLocation = line.substringAfter("REG_SZ").trim()
                        }
                        line.contains("DisplayIcon") && line.contains("REG_SZ") -> {
                            displayIcon = line.substringAfter("REG_SZ").trim()
                        }
                        line.contains("UninstallString") && line.contains("REG_SZ") -> {
                            uninstallString = line.substringAfter("REG_SZ").trim()
                        }
                    }
                }

                // Only add if it's a valid user application
                if (!displayName.isNullOrBlank() &&
                    isValidUserApplication(displayName!!) &&
                    !isSystemComponent(displayName!!, uninstallString)) {

                    val executablePath = findExecutableFromRegistry(installLocation, displayIcon, uninstallString)

                    apps.add(
                        AppInfo(
                            displayName!!,
                            executablePath ?: installLocation ?: "",
                            "",
                            ""
                        )
                    )
                    println("[SERVER] Found registry app: $displayName")
                }
            } catch (e: Exception) {
                // Skip malformed entries
            }
        }
    }

    private fun findExecutableFromRegistry(installLocation: String?, displayIcon: String?, uninstallString: String?): String? {
        // Try to find the main executable from registry data
        displayIcon?.let { icon ->
            if (icon.endsWith(".exe", ignoreCase = true) && File(icon).exists()) {
                return icon
            }
        }

        installLocation?.let { location ->
            val locationFile = File(location)
            if (locationFile.exists() && locationFile.isDirectory) {
                // Look for common executable names in install directory
                val commonExeNames = listOf("app.exe", "main.exe", "launcher.exe")
                commonExeNames.forEach { exeName ->
                    val exeFile = File(locationFile, exeName)
                    if (exeFile.exists()) return exeFile.absolutePath
                }

                // Find first .exe file that's not an uninstaller
                locationFile.listFiles { file ->
                    file.extension.lowercase() == "exe" &&
                            !file.nameWithoutExtension.lowercase().contains("uninstall")
                }?.firstOrNull()?.let { return it.absolutePath }
            }
        }

        return null
    }

    private fun isValidUserApplication(appName: String): Boolean {
        val lowerName = appName.lowercase()

        // Exclude system components and updates
        val excludePatterns = listOf(
            "microsoft visual c++",
            "microsoft .net",
            "windows sdk",
            "redistributable",
            "runtime",
            "update for",
            "hotfix for",
            "security update",
            "service pack",
            "kb[0-9]+",
            "uninstall",
            "driver",
            "codec",
            "directx",
            "vcredist",
            "dotnet",
            "framework"
        )

        return !excludePatterns.any { pattern ->
            if (pattern.contains("[0-9]")) {
                lowerName.matches(Regex(".*${pattern.replace("[0-9]+", "\\d+")}.*"))
            } else {
                lowerName.contains(pattern)
            }
        }
    }

    private fun isSystemComponent(displayName: String, uninstallString: String?): Boolean {
        val lowerName = displayName.lowercase()
        val lowerUninstall = uninstallString?.lowercase() ?: ""

        // Check if it's a system component
        return lowerName.contains("microsoft") && (
                lowerUninstall.contains("msiexec") ||
                        lowerUninstall.contains("system32") ||
                        lowerName.contains("redistributable") ||
                        lowerName.contains("runtime")
                )
    }

    private fun handleWindowsShortcut(shortcutFile: File, apps: MutableList<AppInfo>) {
        // Only process shortcuts that look like user applications
        val appName = shortcutFile.nameWithoutExtension
        if (isValidUserApplication(appName)) {
            try {
                apps.add(
                    AppInfo(
                        appName,
                        shortcutFile.absolutePath,
                        "",
                        ""
                    )
                )
                println("[SERVER] Found Windows shortcut: $appName")
            } catch (e: Exception) {
                println("[SERVER] Error processing shortcut: ${shortcutFile.name} - ${e.message}")
            }
        }
    }

    private fun getLinuxApplications(apps: MutableList<AppInfo>) {
        val applicationDirectories = listOf(
            "/usr/share/applications",
            "${System.getProperty("user.home")}/.local/share/applications"
        )

        applicationDirectories.forEach { directory ->
            val dirFile = File(directory)
            if (dirFile.exists()) {
                println("[SERVER] Scanning Linux applications in: $directory")

                dirFile.listFiles { file ->
                    file.extension == "desktop"
                }?.forEach { file ->
                    try {
                        parseDesktopFile(file, apps)
                    } catch (e: Exception) {
                        println("[SERVER] Error processing Linux app: ${file.name} - ${e.message}")
                    }
                }
            }
        }
    }

    private fun parseDesktopFile(file: File, apps: MutableList<AppInfo>) {
        val content = file.readText()
        val lines = content.split("\n")

        var name: String? = null
        var exec: String? = null
        var icon: String? = null

        lines.forEach { line ->
            when {
                line.startsWith("Name=") && name == null -> {
                    name = line.substringAfter("Name=")
                }
                line.startsWith("Exec=") -> {
                    exec = line.substringAfter("Exec=")
                }
                line.startsWith("Icon=") -> {
                    icon = line.substringAfter("Icon=")
                }
            }
        }

        if (!name.isNullOrBlank() && !exec.isNullOrBlank()) {
            apps.add(
                AppInfo(
                    name!!,
                    exec!!,
                    icon ?: "",
                    ""
                )
            )
            println("[SERVER] Found Linux app: $name")
        }
    }

    private fun isSystemFile(fileName: String): Boolean {
        val systemFiles = setOf(
            "uninstall", "setup", "install", "updater", "launcher",
            "helper", "service", "daemon", "crashreporter", "unins",
            "config", "diagnostic", "export", "util", "cmd", "srv",
            "mig", "launch", "network", "share", "rph", "eng"
        )
        return systemFiles.any { fileName.contains(it, ignoreCase = true) }
    }
}
