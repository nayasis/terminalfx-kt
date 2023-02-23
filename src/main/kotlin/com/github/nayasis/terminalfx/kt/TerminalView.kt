package com.github.nayasis.terminalfx.kt

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nayasis.kotlin.basica.core.io.delete
import com.github.nayasis.kotlin.basica.core.io.exists
import com.github.nayasis.terminalfx.kt.annotation.WebkitCall
import com.github.nayasis.terminalfx.kt.config.TerminalConfig
import com.github.nayasis.terminalfx.kt.helper.ThreadHelper
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.Pane
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import java.io.IOException
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.CountDownLatch

open class TerminalView(): Pane() {

    private var webView: WebView? = null
    private var columnsProperty: ReadOnlyIntegerWrapper? = null
    private var rowsProperty: ReadOnlyIntegerWrapper? = null
    private var inputReaderProperty: ObjectProperty<Reader>? = null
    private var errorReaderProperty: ObjectProperty<Reader>? = null
    private var terminalConfig: TerminalConfig = TerminalConfig()

    protected val countDownLatch = CountDownLatch(1)

    companion object {
        private var tempDirectory: Path? = null
        init {
            Runtime.getRuntime().addShutdownHook(object: Thread() {
                override fun run() {
                    try {
                        if(tempDirectory.exists()) {
                            tempDirectory!!.delete()
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            })
        }
    }

    init {
        initializeResources()
        webView = WebView()
        columnsProperty = ReadOnlyIntegerWrapper(150)
        rowsProperty = ReadOnlyIntegerWrapper(10)
        inputReaderProperty = SimpleObjectProperty()
        errorReaderProperty = SimpleObjectProperty()
        inputReaderProperty?.addListener(ChangeListener { observable: ObservableValue<out Reader>?, oldValue: Reader?, newValue: Reader ->
            ThreadHelper.start {
                printReader(
                    newValue
                )
            }
        })
        errorReaderProperty?.addListener(ChangeListener { observable: ObservableValue<out Reader>?, oldValue: Reader?, newValue: Reader ->
            ThreadHelper.start {
                printReader(
                    newValue
                )
            }
        })
        webView?.engine?.loadWorker?.stateProperty()?.addListener { _, _, _ ->
                getWindow().setMember( "app", this )
            }
        webView?.prefHeightProperty()?.bind(heightProperty())
        webView?.prefWidthProperty()?.bind(widthProperty())
        val htmlPath = tempDirectory!!.resolve("hterm.html")
        webEngine().load(htmlPath.toUri().toString())
    }

    private fun initializeResources() {
        try {
            if (Objects.isNull(tempDirectory) || Files.notExists(tempDirectory)) {
                tempDirectory = Files.createTempDirectory("TerminalFX_Temp")
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val htmlPath = tempDirectory!!.resolve("hterm.html")
        if (Files.notExists(htmlPath)) {
            try {
                TerminalView::class.java.getResourceAsStream("/hterm.html").use { html ->
                    Files.copy(
                        html,
                        htmlPath,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        val htermJsPath = tempDirectory!!.resolve("hterm_all.js")
        if (Files.notExists(htermJsPath)) {
            try {
                TerminalView::class.java.getResourceAsStream("/hterm_all.js").use { html ->
                    Files.copy(
                        html,
                        htermJsPath,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    @WebkitCall(from = "hterm")
    fun getPrefs(): String {
        return try {
            ObjectMapper().writeValueAsString(getTerminalConfig())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun updatePrefs(terminalConfig: TerminalConfig) {
        if (getTerminalConfig().equals(terminalConfig)) {
            return
        }
        setTerminalConfig(terminalConfig)
        val prefs = getPrefs()
        ThreadHelper.runActionLater({
            try {
                getWindow().call("updatePrefs", prefs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, true)
    }

    @WebkitCall(from = "hterm")
    fun resizeTerminal(columns: Int, rows: Int) {
        columnsProperty!!.set(columns)
        rowsProperty!!.set(rows)
    }

    @WebkitCall
    fun onTerminalInit() {
        ThreadHelper.runActionLater({ children.add(webView) }, true)
    }

    @WebkitCall
    open fun onTerminalReady() {
        ThreadHelper.start {
            try {
                focusCursor()
                countDownLatch.countDown()
            } catch (e: Exception) {
            }
        }
    }

    private fun printReader(bufferedReader: Reader) {
        try {
            var nRead: Int
            val data = CharArray(1 * 1024)
            while (bufferedReader.read(data, 0, data.size).also { nRead = it } != -1) {
                val builder = StringBuilder(nRead)
                builder.append(data, 0, nRead)
                print(builder.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @WebkitCall(from = "hterm")
    fun copy(text: String?) {
        val clipboard = Clipboard.getSystemClipboard()
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(text)
        clipboard.setContent(clipboardContent)
    }

    fun onTerminalFxReady(onReadyAction: Runnable?) {
        ThreadHelper.start {
            ThreadHelper.awaitLatch(countDownLatch)
            if (Objects.nonNull(onReadyAction)) {
                ThreadHelper.start(onReadyAction)
            }
        }
    }

    protected fun print(text: String?) {
        ThreadHelper.awaitLatch(countDownLatch)
        ThreadHelper.runActionLater { getTerminalIO().call("print", text) }
    }

    fun focusCursor() {
        ThreadHelper.runActionLater({
            webView!!.requestFocus()
            getTerminal().call("focus")
        }, true)
    }

    private fun getTerminal(): JSObject {
        return webEngine().executeScript("t") as JSObject
    }

    private fun getTerminalIO(): JSObject {
        return webEngine().executeScript("t.io") as JSObject
    }

    fun getWindow(): JSObject {
        return webEngine().executeScript("window") as JSObject
    }

    private fun webEngine(): WebEngine {
        return webView!!.engine
    }

    fun getTerminalConfig(): TerminalConfig {
        if (Objects.isNull(terminalConfig)) {
            terminalConfig = TerminalConfig()
        }
        return terminalConfig
    }

    fun setTerminalConfig(terminalConfig: TerminalConfig) {
        this.terminalConfig = terminalConfig
    }

    fun columnsProperty(): ReadOnlyIntegerProperty? {
        return columnsProperty!!.readOnlyProperty
    }

    fun getColumns(): Int {
        return columnsProperty!!.get()
    }

    fun rowsProperty(): ReadOnlyIntegerProperty? {
        return rowsProperty!!.readOnlyProperty
    }

    fun getRows(): Int {
        return rowsProperty!!.get()
    }

    fun inputReaderProperty(): ObjectProperty<Reader>? {
        return inputReaderProperty
    }

    fun getInputReader(): Reader? {
        return inputReaderProperty!!.get()
    }

    fun setInputReader(reader: Reader) {
        inputReaderProperty!!.set(reader)
    }

    fun errorReaderProperty(): ObjectProperty<Reader>? {
        return errorReaderProperty
    }

    fun getErrorReader(): Reader? {
        return errorReaderProperty!!.get()
    }

    fun setErrorReader(reader: Reader) {
        errorReaderProperty!!.set(reader)
    }

}