package com.github.nayasis.terminalfx.kt

import com.github.nayasis.kotlin.basica.etc.Platforms
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.github.nayasis.terminalfx.kt.config.toHex
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import tornadofx.launch
import kotlin.system.exitProcess

fun main() {
    launch<TerminalTest>()
}

class TerminalTest: Application() {
    override fun start(stage: Stage?) {

        val config = TerminalConfig().apply {
            commandline = (if(Platforms.isWindows) "cmd.exe" else "/bin/bash -i").split("\\s+")
            cursorColor = "white"
            foregroundColor = Color.rgb(200, 200, 200).toHex()
            backgroundColor = Color.rgb(16, 16, 16).toHex()
            fontSize = 12
            scrollWheelMoveMultiplier = 3.0
            enableClipboardNotice = false
            scrollbarVisible = false
        }

        val terminal = Terminal(config, "merong")

        stage?.apply {
            title  = "TerminalFX"
            width  = 600.0
            height = 400.0
            scene  = Scene(terminal)
            show()
        }

        terminal.onTerminalFxReady { terminal.command("dir\r") }

    }
    override fun stop() {
        Platform.exit()
        exitProcess(0)
    }
}