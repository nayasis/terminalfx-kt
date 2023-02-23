package com.github.nayasis.terminalfx.kt

import com.github.nayasis.kotlin.basica.core.io.copyTo
import com.github.nayasis.kotlin.basica.core.io.delete
import com.github.nayasis.kotlin.basica.core.io.exists
import com.github.nayasis.kotlin.basica.core.io.notExists
import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.kotlin.basica.reflection.Reflector
import com.github.nayasis.kotlin.javafx.misc.Desktop
import com.github.nayasis.kotlin.javafx.misc.set
import com.github.nayasis.terminalfx.kt.annotation.WebkitCall
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import javafx.scene.web.WebView
import mu.KotlinLogging
import netscape.javascript.JSObject
import tornadofx.runAsync
import tornadofx.runLater
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardCopyOption.*
import java.util.concurrent.CountDownLatch

private val logger = KotlinLogging.logger {}

private var tempDirectory: Path? = null

open class TerminalView(
    var terminalConfig: TerminalConfig = TerminalConfig()
): Pane() {

    private val webView = WebView()
    private val inputReaderProperty = SimpleObjectProperty<Reader>()
    private val errorReaderProperty = SimpleObjectProperty<Reader>()

    protected val countDownLatch = CountDownLatch(1)

    var inputReader: Reader
        get() = inputReaderProperty.get()
        set(reader) = inputReaderProperty.set(reader)

    var errorReader: Reader
        get() = errorReaderProperty.get()
        set(reader) = errorReaderProperty.set(reader)

    companion object {
        init {
            Runtime.getRuntime().addShutdownHook(object: Thread() {
                override fun run() {
                    runCatching {
                        if(tempDirectory.exists()) {
                            tempDirectory!!.delete()
                        }
                    }.onFailure { logger.error(it) }
                }
            })
        }
    }

    init {
        prepareHtermResources()
        inputReaderProperty.addListener { _, _, reader -> runAsync {
            printReader( reader )
        }}
        errorReaderProperty.addListener { _, _, reader: Reader -> runAsync {
            printReader( reader )
        }}
        webView.prefHeightProperty().bind(heightProperty())
        webView.prefWidthProperty().bind(widthProperty())
        webView.engine.loadWorker.stateProperty()?.addListener { _, _, _ ->
            window.setMember( "app", this )
        }
        webView.engine.load(tempDirectory!!.resolve("hterm.html").toUri().toString())
    }

    private fun prepareHtermResources() {
        if(tempDirectory.notExists()) {
            tempDirectory = Files.createTempDirectory("TerminalFX_Temp")
        }
        copyResource("hterm.html")
        copyResource("hterm_all.js")
    }

    private fun copyResource(resourceName: String) {
        val file = tempDirectory!!.resolve(resourceName)
        if(file.notExists()) {
            TerminalView::class.java.getResourceAsStream("/$resourceName").use {
                it.copyTo(file, REPLACE_EXISTING)
            }
        }
    }

    @WebkitCall(from = "hterm")
    fun getPrefs(): String {
        return Reflector.toJson(terminalConfig)
    }

    fun updatePrefs(terminalConfig: TerminalConfig) {
        if (this.terminalConfig == terminalConfig) return
        this.terminalConfig = terminalConfig
        runLater {
            window.call("updatePrefs", getPrefs())
        }
    }

    @WebkitCall
    fun onTerminalInit() {
        runLater {
            children.add(webView)
        }
    }

    @WebkitCall
    open fun onTerminalReady() {
        runAsync {
            focusCursor()
            countDownLatch.countDown()
        }
    }

    private fun printReader(reader: Reader) {
        var nRead: Int
        val data = CharArray(1 * 1024)
        runCatching {
            while (reader.read(data, 0, data.size).also { nRead = it } != -1) {
                val sb = StringBuilder(nRead)
                sb.append(data, 0, nRead)
                print(sb.toString())
            }
        }.onFailure { logger.error(it) }
    }

    @WebkitCall(from = "hterm")
    fun copy(text: String?) {
        Desktop.clipboard.set(text)
    }

    fun onTerminalFxReady(action:() -> Unit) {
        runAsync {
            await()
            runAsync {
                action.invoke()
            }
        }
    }

    fun print(text: String?) {
        await()
        runLater {
            terminalIO.call("print", text)
        }
    }

    fun focusCursor() {
        runLater {
            webView.requestFocus()
            terminal.call("focus")
        }
    }

    private val terminal: JSObject
        get() = webView.engine.executeScript("t") as JSObject
    private val terminalIO: JSObject
        get() = webView.engine.executeScript("t.io") as JSObject
    private val window: JSObject
        get() = webView.engine.executeScript("window") as JSObject

    private fun await() {
        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            logger.error(e)
        }
    }

}