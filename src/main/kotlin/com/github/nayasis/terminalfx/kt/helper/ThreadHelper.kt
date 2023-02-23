package com.github.nayasis.terminalfx.kt.helper

import java.util.concurrent.Executors

class ThreadHelper { companion object {

    private val singleExecutorService = Executors.newSingleThreadExecutor()

    // Runs task in JavaFX Thread

    fun start(runnable: Runnable?) {
        val thread = Thread(runnable)
        thread.start()
    }

    fun stopExecutorService() {
        if (!singleExecutorService.isShutdown) {
            singleExecutorService.shutdown()
        }
    }

}}