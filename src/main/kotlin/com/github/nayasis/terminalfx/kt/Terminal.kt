package com.github.nayasis.terminalfx.kt

import com.github.nayasis.kotlin.basica.core.io.exists
import com.github.nayasis.kotlin.basica.core.string.toPath
import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.terminalfx.kt.annotation.WebkitCall
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.pty4j.PtyProcessBuilder
import javafx.beans.property.SimpleObjectProperty
import mu.KotlinLogging
import tornadofx.runAsync
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.concurrent.LinkedBlockingQueue

private val logger = KotlinLogging.logger {}

class Terminal(
    terminalConfig: TerminalConfig = TerminalConfig(),
    private val workingDirectory: String? = null,
): TerminalView(terminalConfig) {

    var process: Process? = null
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
                val c = commandQueue.poll()
                logger.trace { ">> send command : $c" }
                write(c)
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

        process = PtyProcessBuilder(terminalConfig.commandline?.toTypedArray() ?: arrayOf())
            .setEnvironment(getEnvironment())
            .apply {
                if ( workingDirectory?.toPath()?.exists() == true) {
                    setDirectory(workingDirectory)
                }
            }
            .start()

        System.getProperty("file.encoding").let { charset ->
            inputReader  = BufferedReader(InputStreamReader(process!!.inputStream, charset))
            errorReader  = BufferedReader(InputStreamReader(process!!.errorStream, charset))
            outputWriter = BufferedWriter(OutputStreamWriter(process!!.outputStream, charset))
        }

        focusCursor()
        countDownLatch.countDown()
        process!!.waitFor()

    }

    private fun getEnvironment(): Map<String, String> {
        return HashMap(System.getenv()).apply {
            put("TERM", "xterm")
        }
    }

}