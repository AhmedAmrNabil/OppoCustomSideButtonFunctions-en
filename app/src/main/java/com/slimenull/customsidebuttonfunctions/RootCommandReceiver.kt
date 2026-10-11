package com.slimenull.customsidebuttonfunctions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class RootCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getStringExtra(EXTRA_COMMAND)?.takeIf { it.isNotBlank() } ?: return
        if (intent.getBooleanExtra(EXTRA_SHOW_TOAST, true)) {
            Toast.makeText(context.applicationContext, "已收到 Shell 广播", Toast.LENGTH_SHORT).show()
        }
        val pending = goAsync()
        Thread {
            try {
                val success = when (ShizukuShellCommandExecutor.execute(context, command)) {
                    ShizukuShellCommandExecutor.Result.SHIZUKU_SUCCESS -> true
                    ShizukuShellCommandExecutor.Result.FALLBACK_REQUIRED ->
                        ShizukuShellCommandExecutor.executeWithSu(command)
                }
                if (!success) {
                    Log.e(TAG, "root command failed")
                }
            } catch (error: Throwable) {
                Log.e(TAG, "root command failed", error)
            } finally {
                pending.finish()
            }
        }.apply {
            isDaemon = true
            name = "CustomSideButtonRootCommand"
            start()
        }
    }

    companion object {
        const val ACTION = "com.slimenull.customsidebuttonfunctions.EXECUTE_ROOT"
        const val EXTRA_COMMAND = "command"
        const val EXTRA_SHOW_TOAST = "show_toast"
        private const val TAG = "CustomSideButtonFunctions"
    }
}
