package com.harimoradiya.wifimouseserver.model

data class AppInfo(
    val name: String,
    val path: String,
    val iconBase64: String = "",
    val bundleId: String = ""
)
