package com.github.nayasis.terminalfx.kt

import com.github.nayasis.kotlin.basica.core.extention.ifNotEmpty
import com.github.nayasis.kotlin.basica.core.extention.isNotEmpty
import com.github.nayasis.kotlin.basica.core.io.Paths
import com.github.nayasis.kotlin.basica.core.string.toFile
import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.kotlin.basica.exec.Command
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
import java.lang.ProcessBuilder.Redirect.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
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

//    private fun initializeProcess() {
//
//        setPtyLibFolder()
//
//        val commandline = terminalConfig.commandline.split("\\s+").toTypedArray()
//
//        process = if ( workingDirectory.isNotEmpty() && Files.exists(workingDirectory)) {
//            PtyProcess.exec(commandline, getEnvironment(), workingDirectory.toString())
//        } else {
//            PtyProcess.exec(commandline, getEnvironment())
//        }
//
//        val charset = System.getProperty("file.encoding")
//        inputReader  = BufferedReader(InputStreamReader(process!!.inputStream, charset))
//        errorReader  = BufferedReader(InputStreamReader(process!!.errorStream, charset))
//        outputWriter = BufferedWriter(OutputStreamWriter(process!!.outputStream, charset))
//
//        focusCursor()
//        countDownLatch.countDown()
//        process!!.waitFor()
//
//    }

    private fun getEnvironment(): Map<String, String> {
        return HashMap(System.getenv()).apply {
            put("TERM", "xterm")
        }
    }

    private fun setPtyLibFolder() {
        System.setProperty(
            "PTY_LIB_FOLDER",
            Paths.userHome.resolve(".terminalfx/libpty").toString()
        )
    }

    private fun initializeProcess() {

        process = ProcessBuilder(terminalConfig.commandline.split(" \\s+")).apply {
            environment().putAll(mapOf("TERM" to "xterm"))
            workingDirectory?.toFile().ifNotEmpty { if(it.exists()) directory(it) }
            redirectInput(PIPE)
            redirectError(PIPE)
            redirectOutput(DISCARD)
        }.start()

        process = Command(
            cli = terminalConfig.commandline,
            workingDirectory = workingDirectory,
            environment = getEnvironment(),
        ).runProcess(redirectError = false)

        val charset = System.getProperty("file.encoding")
        inputReader  = BufferedReader(InputStreamReader(process!!.inputStream, charset))
        errorReader  = BufferedReader(InputStreamReader(process!!.errorStream, charset))
        outputWriter = BufferedWriter(OutputStreamWriter(process!!.outputStream, charset))

        focusCursor()

        countDownLatch.countDown()
        process!!.waitFor()

    }

}