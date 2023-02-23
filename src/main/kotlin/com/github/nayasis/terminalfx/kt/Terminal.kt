package com.github.nayasis.terminalfx.kt

import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.terminalfx.kt.annotation.WebkitCall
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.pty4j.PtyProcess
import javafx.beans.property.SimpleObjectProperty
import mu.KotlinLogging
import tornadofx.runAsync
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

private val logger = KotlinLogging.logger {}

class Terminal(
    terminalConfig: TerminalConfig = TerminalConfig(),
    val workingDirectory: Path? = null,
): TerminalView(terminalConfig) {

    var process: PtyProcess? = null
        private set

    private val outputWriterProperty = SimpleObjectProperty<Writer>()
    private val commandQueue = LinkedBlockingQueue<String>()

    var outputWriter: Writer
        get() = outputWriterProperty.get()
        set(writer) = outputWriterProperty.set(writer)

    @WebkitCall
    fun command(command: String) {
        commandQueue.put(command)
        runAsync {
            outputWriter.run {
                write(commandQueue.poll())
                flush()
            }
        }
    }

    @WebkitCall
    override fun onTerminalReady() {
        runAsync {
            runCatching {
                initializeProcess()
            }.onFailure { logger.error(it) }
        }
    }

    private fun initializeProcess() {

        val dataDir = getDataDir()
        val termCommand = terminalConfig.terminalCommand.split("\\s+").toTypedArray()

        val envs = HashMap(System.getenv()).apply {
            put("TERM","xterm")
        }
        System.setProperty("PTY_LIB_FOLDER", dataDir.resolve("libpty").toString())
        if (Objects.nonNull(workingDirectory) && Files.exists(workingDirectory)) {
            process = PtyProcess.exec(termCommand, envs, workingDirectory.toString())
        } else {
            process = PtyProcess.exec(termCommand, envs)
        }

        val defaultCharEncoding = System.getProperty("file.encoding")
        inputReader  = BufferedReader(InputStreamReader(process!!.inputStream, defaultCharEncoding))
        errorReader  = BufferedReader(InputStreamReader(process!!.errorStream, defaultCharEncoding))
        outputWriter = BufferedWriter(OutputStreamWriter(process!!.outputStream, defaultCharEncoding))
        focusCursor()
        countDownLatch.countDown()
        process!!.waitFor()
    }

    private fun getDataDir(): Path {
        val userHome = System.getProperty("user.home")
        return Paths.get(userHome).resolve(".terminalfx")
    }

}