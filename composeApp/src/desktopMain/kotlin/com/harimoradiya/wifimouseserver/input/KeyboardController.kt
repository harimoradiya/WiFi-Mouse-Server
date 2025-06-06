package com.harimoradiya.wifimouseserver.input

import java.awt.Robot
import java.awt.event.KeyEvent

object KeyboardController {
    private val robot = try {
        Robot()
    } catch (e: SecurityException) {
        println("Error: Accessibility permissions not granted - ${e.message}")
        null
    }
    private val keyMap = mapOf(
        "ENTER" to KeyEvent.VK_ENTER,
        "BACKSPACE" to KeyEvent.VK_BACK_SPACE,
        "SPACE" to KeyEvent.VK_SPACE,
        "TAB" to KeyEvent.VK_TAB,
        "ESC" to KeyEvent.VK_ESCAPE,
        "UP" to KeyEvent.VK_UP,
        "DOWN" to KeyEvent.VK_DOWN,
        "LEFT" to KeyEvent.VK_LEFT,
        "RIGHT" to KeyEvent.VK_RIGHT,
        "HOME" to KeyEvent.VK_HOME,
        "END" to KeyEvent.VK_END,
        "PAGE_UP" to KeyEvent.VK_PAGE_UP,
        "PAGE_DOWN" to KeyEvent.VK_PAGE_DOWN,
        "DELETE" to KeyEvent.VK_DELETE,
        "INSERT" to KeyEvent.VK_INSERT,
        "F1" to KeyEvent.VK_F1,
        "F2" to KeyEvent.VK_F2,
        "F3" to KeyEvent.VK_F3,
        "F4" to KeyEvent.VK_F4,
        "F5" to KeyEvent.VK_F5,
        "F6" to KeyEvent.VK_F6,
        "F7" to KeyEvent.VK_F7,
        "F8" to KeyEvent.VK_F8,
        "F9" to KeyEvent.VK_F9,
        "F10" to KeyEvent.VK_F10,
        "F11" to KeyEvent.VK_F11,
        "F12" to KeyEvent.VK_F12
    )


    fun type(text: String) {
        if (robot == null) {
            println("Error: Cannot type text - Robot not initialized (missing permissions)")
            return
        }
        for (char in text.toCharArray()) {
            typeCharacter(char)
        }
    }

    fun pressSpecialKey(key: String, modifiers: List<String> = emptyList()) {
        if (robot == null) {
            println("Error: Cannot press special key - Robot not initialized (missing permissions)")
            return
        }
        val modifierKeys = modifiers.mapNotNull { getModifierKey(it) }
        
        // Press modifier keys
        modifierKeys.forEach { robot.keyPress(it) }
        
        // Press and release the main key
        keyMap[key]?.let {
            robot.keyPress(it)
            robot.keyRelease(it)
        }
        
        // Release modifier keys in reverse order
        modifierKeys.reversed().forEach { robot.keyRelease(it) }
    }
    
    private fun getModifierKey(modifier: String): Int? = when (modifier.uppercase()) {
        "CTRL", "CONTROL" -> KeyEvent.VK_CONTROL
        "ALT" -> KeyEvent.VK_ALT
        "SHIFT" -> KeyEvent.VK_SHIFT
        "META", "COMMAND", "WIN" -> KeyEvent.VK_META
        else -> null
    }

    private fun typeCharacter(c: Char) {
        if (robot == null) return
        try {
            val code = KeyEvent.getExtendedKeyCodeForChar(c.code)
            if (code == KeyEvent.VK_UNDEFINED) {
                println("Warning: Undefined key code for character: $c")
                return
            }
            if (Character.isUpperCase(c)) {
                robot.keyPress(KeyEvent.VK_SHIFT)
            }
            robot.keyPress(code)
            Thread.sleep(5) // Add small delay for stability
            robot.keyRelease(code)
            if (Character.isUpperCase(c)) {
                robot.keyRelease(KeyEvent.VK_SHIFT)
            }
        } catch (e: IllegalArgumentException) {
            // Handle invalid character
            println("Handle invalid character - ${e.message}")
        }
    }
}