package com.harimoradiya.wifimouseserver.utils

import java.net.NetworkInterface

fun getLocalIpAddress(): String {
    return try {
        NetworkInterface.getNetworkInterfaces()
            .asSequence()
            .flatMap { it.inetAddresses.asSequence() }
            .firstOrNull { address ->
                !address.isLoopbackAddress && address.hostAddress.contains(".")
            }?.hostAddress ?: "Unknown"
    } catch (ex: Exception) {
        "Unknown"
    }
}