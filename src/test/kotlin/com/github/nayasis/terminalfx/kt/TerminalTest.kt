package com.github.nayasis.terminalfx.kt

import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.github.nayasis.terminalfx.kt.helper.ThreadHelper
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import tornadofx.*
import kotlin.system.exitProcess

fun main() {
    launch<TerminalTest>()
}

class TerminalTest: Application() {
    override fun start(stage: Stage?) {

        val config = TerminalConfig()
        val terminal = Terminal(config, null)

        stage?.apply {
            title = "TerminalFX"
            scene = Scene(terminal)
            show()
        }

        terminal.onTerminalFxReady { terminal.command("dir\r") }

    }
    override fun stop() {
        ThreadHelper.stopExecutorService()
        Platform.exit()
        exitProcess(0)
    }
}