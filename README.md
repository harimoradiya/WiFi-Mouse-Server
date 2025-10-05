# WiFi Mouse Server

A powerful Kotlin Multiplatform server application that enables remote control of your computer via WiFi. Transform your Android device into a wireless mouse and keyboard controller with real-time device monitoring capabilities.

## 🖱️ Client App

**WiFi Mouse Control App** for your Android device to connect and control your computer.

👉 [View Github Repo](https://github.com/harimoradiya/WiFi-Mouse-Android-Client)

---

## Screenshots

| Server App (Mac) |
|-------------|
| ![Home Screen](https://github.com/user-attachments/assets/01865b21-eeb5-49b4-bf17-48e25c9009fb) |



## 🚀 Features

- **Real-time Device Information**: Monitor connected client devices with live status updates
- **Server Management**: Easy start/stop server functionality with configurable settings
- **Network Information**: Display server IP address and port for easy client connection
- **Cross-platform Support**: Built with Kotlin Multiplatform for maximum compatibility
- **Secure Connection**: Local network-based communication for enhanced security
- **Low Latency**: Optimized for responsive mouse and keyboard control

## 📋 Prerequisites

- **Operating System**: Windows, macOS, or Linux
- **Java Runtime**: JDK 11 or higher
- **Network**: WiFi network connection
- **Permissions**: Accessibility permissions (required for mouse/keyboard control)

## 🔧 Installation

### From Source
```bash
git clone https://github.com/yourusername/wifi-mouse-server.git
cd wifi-mouse-server
./gradlew build
./gradlew run
```

## ⚙️ Setup & Configuration

### 1. Grant Accessibility Permissions

#### Windows
1. Go to Settings > Privacy & Security > Accessibility
2. Enable accessibility permissions for WiFi Mouse Server

#### macOS
1. Open System Preferences > Security & Privacy > Privacy
2. Select "Accessibility" from the left sidebar
3. Click the lock icon and enter your password
4. Add WiFi Mouse Server to the list of allowed applications

#### Linux
Accessibility permissions are typically granted automatically. If you encounter issues, ensure your user is in the appropriate groups:
```bash
sudo usermod -a -G input $USER
```

### 2. Firewall Configuration
Ensure your firewall allows connections on the server port (default: 8080). Add an exception for the WiFi Mouse Server application.

## 🖥️ Usage

1. **Launch the Application**
- Run the server executable
- The application will display the main dashboard

2. **Start the Server**
- Click the "Start Server" button
- Note the displayed IP address and port number
- Server status will show as "Running"

3. **Monitor Connections**
- View real-time information about connected clients
- Monitor device details and connection status
- Track active sessions

4. **Connect from Client**
- Use the displayed IP and port in your WiFi Mouse Client app
- Ensure both devices are on the same WiFi network

## 📊 Server Information Display

The server provides comprehensive information including:
- **Server Status**: Running/Stopped indicator
- **IP Address**: Local network IP for client connections
- **Port Number**: Communication port (configurable)
- **Connected Clients**: Real-time list of connected devices
- **Device Information**: Client device names, OS versions, and connection times
- **Network Statistics**: Data transfer rates and connection quality

## 🔒 Security Features

- **Local Network Only**: Server only accepts connections from the local network
- **No Internet Required**: All communication happens locally
- **Permission-based Access**: Requires explicit accessibility permissions
- **Connection Logging**: Track all client connections and activities


### Advanced Configuration
- **Custom Port**: Change the default port if 8080 is in use
- **Connection Limits**: Set maximum number of simultaneous connections
- **Timeout Settings**: Configure client timeout duration
- **Logging Levels**: Adjust logging verbosity for debugging

## 🔧 Troubleshooting

### Common Issues


### Debug Mode
Run with debug logging enabled:
```bash
./wifi-mouse-server --debug
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🐛 Bug Reports & Feature Requests

Please use the [GitHub Issues](../../issues) page to report bugs or request new features.

When reporting bugs, please include:
- Operating system and version
- Server version
- Steps to reproduce the issue
- Any error messages or logs

## 📞 Support

- **Issues**: Report bugs on [GitHub Issues](../../issues)
- **Discussions**: Join conversations in [GitHub Discussions](../../discussions)


## 🏗️ Built With

- [Kotlin Multiplatform](https://kotlinlang.org/multiplatform/) - Cross-platform development
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) - JSON handling
- [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines) - Asynchronous programming

---

Made by [Hari Moradiya](https://github.com/harimoradiya)


