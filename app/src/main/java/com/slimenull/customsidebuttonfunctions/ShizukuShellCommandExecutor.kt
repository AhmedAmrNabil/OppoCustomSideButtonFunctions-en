package com.slimenull.customsidebuttonfunctions

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** Starts the declared UserService and asks it to execute a command with Shizuku privileges. */
internal object ShizukuShellCommandExecutor {
    private const val TAG = "CustomSideButtonFunctions"
    private const val TIMEOUT_SECONDS = 10L
    // Bump this whenever the UserService implementation changes so Shizuku recreates it.
    private const val USER_SERVICE_VERSION = 3

    enum class Result {
        SUCCESS,
        FALLBACK_REQUIRED
    }

    fun execute(context: Context, command: String, preferSu: Boolean = false): Result {
        if (preferSu && executeWithSu(command)) {
            Log.i(TAG, "shell command executed through su (preferred)")
            return Result.SUCCESS
        }
        if (!preferSu && executeWithShizuku(context, command)) {
            return Result.SUCCESS
        }
        if (preferSu) {
            if (executeWithShizuku(context, command)) return Result.SUCCESS
        } else if (executeWithSu(command)) {
            Log.i(TAG, "shell command executed through su fallback")
            return Result.SUCCESS
        }
        return Result.FALLBACK_REQUIRED
    }

    private fun executeWithShizuku(context: Context, command: String): Boolean {
        if (!isAuthorized()) {
            Log.i(TAG, "Shizuku unavailable or not authorized")
            return false
        }

        val args = Shizuku.UserServiceArgs(
            ComponentName(context.packageName, ShizukuShellUserService::class.java.name)
        )
            .version(USER_SERVICE_VERSION)
            .tag("shell-command")
            .daemon(false)
            .debuggable(BuildConfig.DEBUG)
            .processNameSuffix("shell")
        val completed = CountDownLatch(1)
        val released = AtomicBoolean(false)
        var exitCode = Int.MIN_VALUE

        lateinit var connection: ServiceConnection
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Log.i(TAG, "Shizuku UserService connected: ${name.className}")
                runCatching {
                    check(service.pingBinder()) { "UserService binder is dead" }
                    val shell = IShizukuShellService.Stub.asInterface(service)
                    checkNotNull(shell) { "UserService interface is null" }
                    shell.execute(command, object : IShizukuShellCallback.Stub() {
                        override fun onFinished(value: Int) {
                            exitCode = value
                            completed.countDown()
                        }
                    })
                }.onFailure { error ->
                    Log.w(TAG, "failed to send command to Shizuku UserService", error)
                    completed.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.w(TAG, "Shizuku UserService disconnected")
                completed.countDown()
            }
        }

        return try {
            Log.i(TAG, "binding Shizuku UserService version=$USER_SERVICE_VERSION")
            Shizuku.bindUserService(args, connection)
            val finished = completed.await(TIMEOUT_SECONDS + 2L, TimeUnit.SECONDS)
            if (finished && exitCode == 0) {
                Log.i(TAG, "shell command executed through Shizuku UserService")
                true
            } else {
                Log.w(TAG, "Shizuku UserService command failed or timed out: exitCode=$exitCode")
                false
            }
        } catch (error: Throwable) {
            Log.w(TAG, "Shizuku UserService unavailable", error)
            false
        } finally {
            if (released.compareAndSet(false, true)) {
                runCatching { Shizuku.unbindUserService(args, connection, true) }
            }
        }
    }

    private fun executeWithSu(command: String): Boolean = runCatching {
        ShellCommandProcess.waitForSuccess(
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)),
            "su"
        )
    }.getOrElse { error ->
        Log.e(TAG, "su command failed", error)
        false
    }

    private fun isAuthorized(): Boolean = runCatching {
        Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)
}
