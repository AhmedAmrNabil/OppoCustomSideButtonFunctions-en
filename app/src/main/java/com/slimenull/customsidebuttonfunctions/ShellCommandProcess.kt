package com.slimenull.customsidebuttonfunctions

import android.util.Log
import java.io.InputStream
import java.util.concurrent.TimeUnit

/** Shared process handling for the Shizuku UserService and the legacy su fallback. */
internal object ShellCommandProcess {
    private const val TAG = "CustomSideButtonFunctions"
    private const val TIMEOUT_SECONDS = 10L
    private const val STREAM_BUFFER_SIZE = 4096
    private const val STREAM_DRAIN_JOIN_MILLIS = 1000L

    fun execute(command: String): Int {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        return waitFor(process, "Shizuku UserService")
    }

    fun waitForSuccess(process: Process, backend: String): Boolean = waitFor(process, backend) == 0

    private fun waitFor(process: Process, backend: String): Int {
        val stdout = drainAsync(process.inputStream)
        val stderr = drainAsync(process.errorStream)
        val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (!finished) {
            Log.e(TAG, "$backend command timed out")
            process.destroyForcibly()
            stdout.join(STREAM_DRAIN_JOIN_MILLIS)
            stderr.join(STREAM_DRAIN_JOIN_MILLIS)
            return -1
        }
        stdout.join(STREAM_DRAIN_JOIN_MILLIS)
        stderr.join(STREAM_DRAIN_JOIN_MILLIS)
        val exitCode = process.exitValue()
        Log.i(TAG, "$backend command exited with code $exitCode")
        return exitCode
    }

    private fun drainAsync(stream: InputStream): Thread = Thread {
        runCatching {
            stream.use { input ->
                val buffer = ByteArray(STREAM_BUFFER_SIZE)
                while (input.read(buffer) != -1) Unit
            }
        }
    }.apply {
        isDaemon = true
        start()
    }
}
