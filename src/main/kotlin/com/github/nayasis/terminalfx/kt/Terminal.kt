package com.github.nayasis.terminalfx.kt

import com.github.nayasis.kotlin.basica.core.extention.isNotEmpty
import com.github.nayasis.kotlin.basica.core.io.exists
import com.github.nayasis.kotlin.basica.core.string.toPath
import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.terminalfx.kt.annotation.WebkitCall
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.github.nayasis.terminalfx.kt.config.TerminalSize
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import com.pty4j.WinSize
import javafx.beans.property.ReadOnlyIntegerWrapper
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

@Suppress("MemberVisibilityCanBePrivate")
class Terminal(
    config: TerminalConfig = TerminalConfig(),
    val command: List<String>,
    val workingDirectory: String? = null,
    val environments: Map<String,String> = mapOf("TERM" to "xterm"),
    var onDone:((terminal: Terminal) -> Unit)? = null,
    var onFail: ((terminal: Terminal, error: Throwable) -> Unit)? = null,
    var onSuccess: ((terminal: Terminal, exitValue: Int) -> Unit)? = null,
): TerminalView(config) {

    var process: PtyProcess? = null
        private set

    private val commandQueue    = LinkedBlockingQueue<String>()
    private val columnsProperty = ReadOnlyIntegerWrapper(config.size!!.columns)
    private val rowsProperty    = ReadOnlyIntegerWrapper(config.size!!.rows)

    private val outputWriterProperty = SimpleObjectProperty<Writer?>()
    var outputWriter: Writer?
        get() = outputWriterProperty.get()
        set(writer) = outputWriterProperty.set(writer)

    val terminalSize: TerminalSize
        get() = TerminalSize(columnsProperty.get(), rowsProperty.get())

    @WebkitCall(from = "hterm")
    fun resizeTerminal(columns: Int, rows: Int) {
        columnsProperty.set(columns)
        rowsProperty.set(rows)
    }

    @WebkitCall
    fun command(command: String) {
        commandQueue.put(command)
        runAsync {
            outputWriter?.run {
                runCatching {
                    write(commandQueue.poll())
                    flush()
                }
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
        try {
            process = PtyProcessBuilder(command.toTypedArray())
                .setEnvironment(getEnvironment())
                .apply {
                    if ( workingDirectory?.toPath()?.exists() == true) {
                        setDirectory(workingDirectory)
                    }
                }
                .start()

            columnsProperty.addListener { _ -> updateTerminalSize() }
            rowsProperty.addListener { _ -> updateTerminalSize() }
            updateTerminalSize()

            System.getProperty("file.encoding").let { charset ->
                inputReader  = BufferedReader(InputStreamReader(process!!.inputStream, charset))
                errorReader  = BufferedReader(InputStreamReader(process!!.errorStream, charset))
                outputWriter = BufferedWriter(OutputStreamWriter(process!!.outputStream, charset))
            }

            focusCursor()
            countDownLatch.countDown()
            val exitValue = process!!.waitFor()
            runCatching {
                onSuccess?.invoke(this, exitValue)
            }
        } catch (e: Throwable) {
            runCatching {
                onFail?.invoke(this, e)
            }
        } finally {
            runCatching {
                onDone?.invoke(this)
            }
        }
    }

    fun updateTerminalSize() {
        process?.winSize = WinSize(columnsProperty.get(), rowsProperty.get())
    }

    fun close() {
        runCatching {
            process?.destroy()
            process = null
        }
        runCatching { inputReader?.close() }
        runCatching { errorReader?.close() }
        runCatching { outputWriter?.close() }
    }

    private fun getEnvironment(): Map<String, String> {
        return HashMap(System.getenv()).apply {
            if(environments.isNotEmpty())
                putAll(environments)
        }
    }

}