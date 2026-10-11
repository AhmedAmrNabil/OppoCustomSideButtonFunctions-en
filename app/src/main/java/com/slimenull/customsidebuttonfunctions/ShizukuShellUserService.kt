package com.slimenull.customsidebuttonfunctions

import android.util.Log

/**
 * Binder UserService launched by Shizuku as UID 0 or 2000.
 *
 * A Shizuku UserService is an AIDL Stub directly. It is not an Android Service and must not be
 * registered in the application manifest.
 */
class ShizukuShellUserService : IShizukuShellService.Stub() {
    override fun execute(command: String, callback: IShizukuShellCallback) {
        Thread {
            val exitCode = runCatching {
                ShellCommandProcess.execute(command)
            }.getOrElse { error ->
                Log.e(TAG, "Shizuku UserService command failed", error)
                -1
            }
            runCatching { callback.onFinished(exitCode) }
        }.apply {
            isDaemon = true
            name = "CustomSideButtonShizukuShell"
            start()
        }
    }

    override fun destroy() {
        // Shizuku sends this transaction when unbindUserService(..., remove = true) is used.
        System.exit(0)
    }

    companion object {
        private const val TAG = "CustomSideButtonFunctions"
    }
}
