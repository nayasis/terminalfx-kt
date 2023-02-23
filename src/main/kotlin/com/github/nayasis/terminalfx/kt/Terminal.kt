package com.github.nayasis.terminalfx.kt

import com.github.nayasis.kotlin.basica.core.extention.isNotEmpty
import com.github.nayasis.kotlin.basica.core.io.exists
import com.github.nayasis.kotlin.basica.core.string.toPath
import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.terminalfx.kt.annotation.WebkitCall
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.pty4j.PtyProcessBuilder
import javafx.beans.property.SimpleObjectProperty
import mu.KotlinLogging
import tornadofx.runAsync
import tornadofx.runLater
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

        try {
            process = PtyProcessBuilder(command.toTypedArray())
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
            val exitValue = process!!.waitFor()
            runLater {
                onSuccess?.invoke(this, exitValue)
            }
        } catch (e: Throwable) {
            runLater {
                onFail?.invoke(this, e)
            }
        } finally {
            runLater {
                onDone?.invoke(this)
            }
        }

    }

    private fun getEnvironment(): Map<String, String> {
        return HashMap(System.getenv()).apply {
            if(environments.isNotEmpty())
                putAll(environments)
        }
    }

}