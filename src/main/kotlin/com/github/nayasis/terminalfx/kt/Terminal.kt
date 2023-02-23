package com.github.nayasis.terminalfx.kt

import com.github.nayasis.terminalfx.kt.annotation.WebkitCall
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.github.nayasis.terminalfx.kt.helper.ThreadHelper
import com.pty4j.PtyProcess
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class Terminal(
    terminalConfig: TerminalConfig = TerminalConfig(),
    val terminalPath: Path? = null,
): TerminalView(terminalConfig) {

    private var process: PtyProcess? = null
    private var outputWriterProperty: ObjectProperty<Writer>? = null
    private var commandQueue: LinkedBlockingQueue<String>? = null

    init {
        outputWriterProperty = SimpleObjectProperty()
        commandQueue = LinkedBlockingQueue()
    }

    @WebkitCall
    fun command(command: String) {
        try {
            commandQueue!!.put(command)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        ThreadHelper.start {
            try {
                val commandToExecute = commandQueue!!.poll()
                getOutputWriter().write(commandToExecute)
                getOutputWriter().flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onTerminalReady() {
        ThreadHelper.start {
            try {
                initializeProcess()
            } catch (e: Exception) {
            }
        }
    }

    private fun initializeProcess() {

        val dataDir = getDataDir()
        val termCommand = terminalConfig.terminalCommand.split("\\s+").toTypedArray()

        val envs = HashMap(System.getenv()).apply {
            put("TERM","xterm")
        }
        System.setProperty("PTY_LIB_FOLDER", dataDir.resolve("libpty").toString())
        if (Objects.nonNull(terminalPath) && Files.exists(terminalPath)) {
            process = PtyProcess.exec(termCommand, envs, terminalPath.toString())
        } else {
            process = PtyProcess.exec(termCommand, envs)
        }

        val defaultCharEncoding = System.getProperty("file.encoding")
        inputReader = BufferedReader(InputStreamReader(process!!.inputStream, defaultCharEncoding))
        errorReader = BufferedReader(InputStreamReader(process!!.errorStream, defaultCharEncoding))
        setOutputWriter(BufferedWriter(OutputStreamWriter(process!!.outputStream, defaultCharEncoding)))
        focusCursor()
        countDownLatch.countDown()
        process!!.waitFor()
    }

    private fun getDataDir(): Path {
        val userHome = System.getProperty("user.home")
        return Paths.get(userHome).resolve(".terminalfx")
    }

    fun outputWriterProperty(): ObjectProperty<Writer>? {
        return outputWriterProperty
    }

    fun getOutputWriter(): Writer {
        return outputWriterProperty!!.get()
    }

    fun setOutputWriter(writer: Writer) {
        outputWriterProperty!!.set(writer)
    }

    fun getProcess(): PtyProcess? {
        return process
    }

}