package com.github.nayasis.terminalfx.kt.config

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.*
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.scene.paint.Color
import java.util.*
import kotlin.reflect.full.memberProperties

@JsonInclude(Include.NON_NULL)
class TerminalConfig {

    @JsonProperty("use-default-window-copy")
    private var useDefaultWindowCopy = true

    @JsonProperty("clear-selection-after-copy")
    private var clearSelectionAfterCopy = true

    @JsonProperty("copy-on-select")
    private var copyOnSelect = false

    @JsonProperty("ctrl-c-copy")
    private var ctrlCCopy = true

    @JsonProperty("ctrl-v-paste")
    private var ctrlVPaste = true

    @JsonProperty("cursor-color")
    private var cursorColor = "black"

    @JsonProperty(value = "background-color")
    private var backgroundColor = "white"

    @JsonProperty("font-size")
    private var fontSize = 14

    @JsonProperty(value = "foreground-color")
    private var foregroundColor = "black"

    @JsonProperty("cursor-blink")
    private var cursorBlink = false

    @JsonProperty("scrollbar-visible")
    private var scrollbarVisible = true

    @JsonProperty("enable-clipboard-notice")
    private var enableClipboardNotice = true

    @JsonProperty("scroll-wheel-move-multiplier")
    private var scrollWhellMoveMultiplier = 0.1

    @JsonProperty("font-family")
    private var fontFamily = "\"DejaVu Sans Mono\", \"Everson Mono\", FreeMono, \"Menlo\", \"Terminal\", monospace"

    @JsonProperty(value = "user-css")
    private var userCss = "data:text/plain;base64," + "eC1zY3JlZW4geyBjdXJzb3I6IGF1dG87IH0="

    @JsonIgnore
    private var windowsTerminalStarter = "cmd.exe"

    @JsonIgnore
    private var unixTerminalStarter = "/bin/bash -i"

    fun isUseDefaultWindowCopy(): Boolean {
        return useDefaultWindowCopy
    }

    fun setUseDefaultWindowCopy(useDefaultWindowCopy: Boolean) {
        this.useDefaultWindowCopy = useDefaultWindowCopy
    }

    fun isClearSelectionAfterCopy(): Boolean {
        return clearSelectionAfterCopy
    }

    fun setClearSelectionAfterCopy(clearSelectionAfterCopy: Boolean) {
        this.clearSelectionAfterCopy = clearSelectionAfterCopy
    }

    fun isCopyOnSelect(): Boolean {
        return copyOnSelect
    }

    fun setCopyOnSelect(copyOnSelect: Boolean) {
        this.copyOnSelect = copyOnSelect
    }

    fun isCtrlCCopy(): Boolean {
        return ctrlCCopy
    }

    fun setCtrlCCopy(ctrlCCopy: Boolean) {
        this.ctrlCCopy = ctrlCCopy
    }

    fun isCtrlVPaste(): Boolean {
        return ctrlVPaste
    }

    fun setCtrlVPaste(ctrlVPaste: Boolean) {
        this.ctrlVPaste = ctrlVPaste
    }

    fun getCursorColor(): String? {
        return cursorColor
    }

    fun setCursorColor(cursorColor: String) {
        this.cursorColor = cursorColor
    }

    fun getBackgroundColor(): String? {
        return backgroundColor
    }

    fun setBackgroundColor(backgroundColor: String) {
        this.backgroundColor = backgroundColor
    }

    fun getFontSize(): Int {
        return fontSize
    }

    fun setFontSize(fontSize: Int) {
        this.fontSize = fontSize
    }

    fun getForegroundColor(): String? {
        return foregroundColor
    }

    fun setForegroundColor(foregroundColor: String) {
        this.foregroundColor = foregroundColor
    }

    fun isCursorBlink(): Boolean {
        return cursorBlink
    }

    fun setCursorBlink(cursorBlink: Boolean) {
        this.cursorBlink = cursorBlink
    }

    fun isScrollbarVisible(): Boolean {
        return scrollbarVisible
    }

    fun setScrollbarVisible(scrollbarVisible: Boolean) {
        this.scrollbarVisible = scrollbarVisible
    }

    fun getScrollWhellMoveMultiplier(): Double {
        return scrollWhellMoveMultiplier
    }

    fun setScrollWhellMoveMultiplier(scrollWhellMoveMultiplier: Double) {
        this.scrollWhellMoveMultiplier = scrollWhellMoveMultiplier
    }

    fun getUserCss(): String? {
        return userCss
    }

    fun setUserCss(userCss: String) {
        this.userCss = userCss
    }

    fun getWindowsTerminalStarter(): String? {
        return windowsTerminalStarter
    }

    fun setWindowsTerminalStarter(windowsTerminalStarter: String) {
        this.windowsTerminalStarter = windowsTerminalStarter
    }

    fun getUnixTerminalStarter(): String? {
        return unixTerminalStarter
    }

    fun setUnixTerminalStarter(unixTerminalStarter: String) {
        this.unixTerminalStarter = unixTerminalStarter
    }

    fun setBackgroundColor(color: Color) {
        setBackgroundColor(colorToHex(color))
    }

    fun setForegroundColor(color: Color) {
        setForegroundColor(colorToHex(color))
    }

    fun setCursorColor(color: Color) {
        setCursorColor(colorToHex(color))
    }

    fun getFontFamily(): String? {
        return fontFamily
    }

    fun setFontFamily(fontFamily: String) {
        this.fontFamily = fontFamily
    }

    fun isEnableClipboardNotice(): Boolean {
        return enableClipboardNotice
    }

    fun setEnableClipboardNotice(enableClipboardNotice: Boolean) {
        this.enableClipboardNotice = enableClipboardNotice
    }

    override fun equals(other: Any?) = kotlinEquals(other,TerminalConfig::class.memberProperties.toTypedArray())

    override fun hashCode(): Int = kotlinHashCode(properties = TerminalConfig::class.memberProperties.toTypedArray())

    private fun colorToHex(color: Color): String {
        return String.format(
            "#%02X%02X%02X",
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
    }

}