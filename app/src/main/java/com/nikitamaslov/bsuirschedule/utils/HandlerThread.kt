package com.nikitamaslov.bsuirschedule.utils

import android.os.Handler
import android.os.Looper
import android.os.HandlerThread as BaseHandlerThread

class HandlerThread : BaseHandlerThread("worker_thread"){

    private lateinit var uiHandler: Handler
    private lateinit var workerHandler: Handler

    override fun start() {
        super.start()
        uiHandler = Handler(Looper.getMainLooper())
        workerHandler = Handler(looper)
    }

    fun onWorker(action: () -> Unit) {
        workerHandler.post(action)
    }

    fun onWorker(runnable: Runnable) {
        workerHandler.post(runnable)
    }

    fun onWorkerDelayed(action: () -> Unit, delayMillis: Long) {
        workerHandler.postDelayed(action, delayMillis)
    }

    fun onWorkerDelayed(runnable: Runnable, delayMillis: Long) {
        workerHandler.postDelayed(runnable, delayMillis)
    }

    fun onUi(action: () -> Unit) {
        uiHandler.post(action)
    }

    fun onUi(runnable: Runnable) {
        uiHandler.post(runnable)
    }

    fun onUiDelayed(action: () -> Unit, delayMillis: Long) {
        uiHandler.postDelayed(action, delayMillis)
    }

    fun onUiDelayed(runnable: Runnable, delayMillis: Long) {
        uiHandler.postDelayed(runnable, delayMillis)
    }

    override fun quit(): Boolean {
        workerHandler.removeCallbacksAndMessages(null)
        uiHandler.removeCallbacksAndMessages(null)
        return super.quit()
    }

    fun useOnlyProxy(): UseOnlyProxy = UseOnlyProxy()

    inner class UseOnlyProxy {

        fun onWorker(action: () -> Unit) =
            this@HandlerThread.onWorker(action)

        fun onWorker(runnable: Runnable) =
            this@HandlerThread.onWorker(runnable)

        fun onWorkerDelayed(action: () -> Unit, delayMillis: Long) =
            this@HandlerThread.onWorkerDelayed(action, delayMillis)

        fun onWorkerDelayed(runnable: Runnable, delayMillis: Long) =
            this@HandlerThread.onWorkerDelayed(runnable, delayMillis)

        fun onUi(action: () -> Unit) =
            this@HandlerThread.onUi(action)

        fun onUi(runnable: Runnable) =
            this@HandlerThread.onUi(runnable)

        fun onUiDelayed(action: () -> Unit, delayMillis: Long) =
            this@HandlerThread.onUiDelayed(action, delayMillis)

        fun onUiDelayed(runnable: Runnable, delayMillis: Long) =
            this@HandlerThread.onUiDelayed(runnable, delayMillis)

    }

}