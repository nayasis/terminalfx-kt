package com.github.nayasis.terminalfx.kt.helper

import java.io.Closeable
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class IOHelper { companion object {

    fun close(vararg closables: Closeable) {
        for (closable in closables) {
            try {
                closable.close()
            } catch (e: Exception) {
            }
        }
    }

    @Throws(IOException::class)
    fun copyLibPty(dataDir: Path) {
        val donePath = dataDir.resolve(".DONE")
        if (Files.exists(donePath)) {
            return
        }
        val nativeFiles = getNativeFiles()
        for (nativeFile in nativeFiles) {
            val nativePath = dataDir.resolve(nativeFile)
            if (Files.notExists(nativePath)) {
                Files.createDirectories(nativePath.parent)
                val inputStream = IOHelper::class.java.getResourceAsStream("/$nativeFile")
                Files.copy(inputStream, nativePath)
                close(inputStream)
            }
        }
        Files.createFile(donePath)
    }

    private fun getNativeFiles(): Set<String> {
        val nativeFiles: MutableSet<String> = HashSet()
        val freebsd: List<String> = mutableListOf("libpty/freebsd/x86/libpty.so", "libpty/freebsd/x86_64/libpty.so")
        val linux: List<String> = mutableListOf("libpty/linux/x86/libpty.so", "libpty/linux/x86_64/libpty.so")
        val macosx: List<String> = mutableListOf("libpty/macosx/x86/libpty.dylib", "libpty/macosx/x86_64/libpty.dylib")
        val win_x86: List<String> = mutableListOf("libpty/win/x86/winpty.dll", "libpty/win/x86/winpty-agent.exe")
        val win_x86_64: List<String> = mutableListOf(
            "libpty/win/x86_64/winpty.dll",
            "libpty/win/x86_64/winpty-agent.exe",
            "libpty/win/x86_64/cyglaunch.exe"
        )
        val win_xp: List<String> = mutableListOf("libpty/win/xp/winpty.dll", "libpty/win/xp/winpty-agent.exe")
        nativeFiles.addAll(freebsd)
        nativeFiles.addAll(linux)
        nativeFiles.addAll(macosx)
        nativeFiles.addAll(win_x86)
        nativeFiles.addAll(win_x86_64)
        nativeFiles.addAll(win_xp)
        return nativeFiles
    }

}}