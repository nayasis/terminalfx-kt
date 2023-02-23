package com.github.nayasis.terminalfx.kt.config

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.*
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.scene.paint.Color
import java.util.*
import kotlin.reflect.full.memberProperties

@JsonInclude(Include.NON_NULL)
class TerminalConfig {

    @JsonProperty("use-default-window-copy")
    var useDefaultWindowCopy = true

    @JsonProperty("clear-selection-after-copy")
    var clearSelectionAfterCopy = true

    @JsonProperty("copy-on-select")
    var copyOnSelect = true

    @JsonProperty("ctrl-c-copy")
    var ctrlCCopy = false

    @JsonProperty("ctrl-v-paste")
    var ctrlVPaste = false

    @JsonProperty("cursor-color")
    var cursorColor = "black"

    @JsonProperty(value = "background-color")
    var backgroundColor = "white"

    @JsonProperty("font-size")
    var fontSize = 14

    @JsonProperty(value = "foreground-color")
    var foregroundColor = "black"

    @JsonProperty("cursor-blink")
    var cursorBlink = false

    @JsonProperty("scrollbar-visible")
    var scrollbarVisible = true

    @JsonProperty("enable-clipboard-notice")
    var enableClipboardNotice = true

    @JsonProperty("scroll-wheel-move-multiplier")
    var scrollWheelMoveMultiplier = 0.5

    @JsonProperty("font-family")
    var fontFamily = """
         "DejaVu Sans Mono", "Everson Mono", FreeMono, "Menlo", "Terminal", monospace
       """.trim()

    @JsonProperty(value = "user-css")
    var userCss = "data:text/plain;base64,eC1zY3JlZW4geyBjdXJzb3I6IGF1dG87IH0="

    override fun equals(other: Any?) = kotlinEquals(other,TerminalConfig::class.memberProperties.toTypedArray())

    override fun hashCode(): Int = kotlinHashCode(properties = TerminalConfig::class.memberProperties.toTypedArray())

}

fun Color.toHex(): String {
    return "#%02X%02X%02X".format(
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}