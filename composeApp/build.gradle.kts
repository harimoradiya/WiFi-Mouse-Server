
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
    


    sourceSets {
        val desktopMain by getting

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)

        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.server.netty)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.harimoradiya.wifimouseserver.MainKt"

        nativeDistributions {

                // 1. Fix naming conflicts
                packageName = "WiFi Mouse Server" // Display name (2-3 words)
                packageVersion = "1.0.0"
                description = "Remote Control Server for WiFi Mouse"
                vendor = "WhatsBug"

                // 2. Proper macOS configuration
                macOS {
                    bundleID = "com.harimoradiya.wifimouseserver" // Must match main package
                    dockName = "WiFi Mouse" // Name in Dock
                    appCategory = "public.app-category.utilities"
                    runtimeEntitlementsFile.set(project.file("entitlements.plist"))
                    pkgPackageVersion = "1.0.0"
                    pkgPackageBuildVersion = "1.0.0"
                    // 3. Required signing config (even for development)
                    signing {
                        sign.set(false)
//                        identity.set("Apple Development: Hari Moradiya")
                    }
                }

                // 4. Build targets
                targetFormats(
                    TargetFormat.Dmg,
                    TargetFormat.Pkg
                )


        }
    }
}
