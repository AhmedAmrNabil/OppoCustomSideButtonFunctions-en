package com.slimenull.customsidebuttonfunctions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class RootCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getStringExtra(EXTRA_COMMAND)?.takeIf { it.isNotBlank() } ?: return
        val preferSu = intent.getBooleanExtra(EXTRA_PREFER_SU, false)
        if (intent.getBooleanExtra(EXTRA_SHOW_TOAST, true)) {
            Toast.makeText(context.applicationContext, context.getString(R.string.shell_broadcast_received), Toast.LENGTH_SHORT).show()
        }
        val pending = goAsync()
        Thread {
            try {
                val success = ShizukuShellCommandExecutor.execute(context, command, preferSu) ==
                    ShizukuShellCommandExecutor.Result.SUCCESS
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
        const val EXTRA_PREFER_SU = "prefer_su"
        private const val TAG = "CustomSideButtonFunctions"
    }
}
