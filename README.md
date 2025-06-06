# 🖥WiFi Mouse Server (Kotlin Multiplatform)

This is the **WiFi Mouse Server** built using **Kotlin Multiplatform** that allows an Android device to remotely control a computer’s mouse, keyboard, and access installed apps over the local Wi-Fi network.

The server provides real-time system information, manages client connections, and exposes a WebSocket-based interface to communicate with the Android client.

---

## Features

-  **Start/Stop Server** functionality
-  **Real-time Client Device Info** display
- Shows **Server IP Address and Port**
-  Requires **Accessibility Permission** for full control
- Supports cross-platform devices (macOS, Windows, Linux)

---

## 📷 Preview


---

## ⚙️ Requirements

- Java 11+
- Kotlin Multiplatform Setup
- Accessibility access enabled (for keyboard/mouse control)
- Device and Android client connected on the same Wi-Fi network

---

## 🛠️ How to Use

1. Clone this repository and open it in IntelliJ IDEA.
2. Grant accessibility permission on your OS.
3. Run the server using:

```bash
./gradlew run

```
🔐 Accessibility Permission
This application needs accessibility permission to perform mouse/keyboard actions. Make sure to enable it in your system settings:

macOS: System Preferences → Security & Privacy → Accessibility

Windows: Run app as Administrator and grant input access

🧰 Tech Stack
Kotlin Multiplatform (JVM backend)

WebSocket server (Custom sockets)

Java AWT Robot for mouse & keyboard simulation

System info APIs


This is a Kotlin Multiplatform project targeting Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

