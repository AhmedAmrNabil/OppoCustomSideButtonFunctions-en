package com.slimenull.customsidebuttonfunctions.xposed

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import com.slimenull.customsidebuttonfunctions.model.AppSettings
import com.slimenull.customsidebuttonfunctions.model.CursorControlMode
import com.slimenull.customsidebuttonfunctions.model.CursorLongPressAction
import com.slimenull.customsidebuttonfunctions.model.OperationMode
import com.slimenull.customsidebuttonfunctions.onXposedFailure

/**
 * Converts volume key events to DPAD events while a real IME window is visible.
 *
 * The conversion lives in system_server so it still works on ColorOS builds that
 * consume volume events before the focused InputMethodService sees them.
 */
internal class CursorControlController {
    private companion object {
        const val TAG = "CustomSideButtonFunctions"
        const val IME_VISIBLE = 2
        const val CURSOR_CHORD_DELAY_MS = 180L
        // Hidden WindowManager.LayoutParams values: status-bar panel, notification shade and
        // status-bar additional windows. The title/package checks cover vendor-specific types.
        val SYSTEM_UI_PANEL_TYPES = setOf(2000 + 14, 2000 + 17, 2000 + 40, 2000 + 41)
    }

    private val cursorLock = Any()
    private var context: Context? = null
    private var policyHandler: Handler? = null
    private var inputMethodManagerService: Any? = null
    private var windowManagerService: Any? = null
    @Volatile private var imeWindowVisible = false
    private var imeVisibilityReadErrorLogged = false
    private var focusedWindowReadErrorLogged = false
    private var lastFocusedWindowDescription: String? = null
    private var windowManagerUnavailableLogged = false
    private var windowManagerClassUnavailableLogged = false

    private var powerKeyPressed = false
    private var volumeUpPressed = false
    private var volumeDownPressed = false
    private var cursorChordActive = false
    private data class CursorPress(
        val keyCode: Int,
        val mode: CursorControlMode,
        val longPressAction: CursorLongPressAction,
        val longPressMs: Long,
        val repeatIntervalMs: Long,
        val downEvent: KeyEvent,
        var handler: Handler? = null,
        var initialCursorDownDispatched: Boolean = false,
        var initialCursorReleased: Boolean = false,
        var longTriggered: Boolean = false,
        var longRunnable: Runnable? = null,
        var repeatRunnable: Runnable? = null
    )

    private var volumeUpPress: CursorPress? = null
    private var volumeDownPress: CursorPress? = null
    private var pendingCursorDown: KeyEvent? = null
    private var pendingCursorGeneration = 0

    fun install(lpparam: PackageHookParam) {
        hookWindowManager(lpparam)
        hookPolicy(lpparam)
        hookImeVisibility(lpparam)
        hookMediaVolumePaths(lpparam)
        XposedBridge.log("$TAG: installed volume-key cursor-control hooks")
    }

    private fun hookPolicy(lpparam: PackageHookParam) {
        val classNames = listOf(
            "com.android.server.policy.PhoneWindowManager",
            "com.android.server.policy.PhoneWindowManagerExt"
        )
        for (className in classNames) {
            if (runCatching {
                    XposedHelpers.hookMethod(
                        className,
                        lpparam.classLoader,
                        "interceptKeyBeforeQueueing",
                        KeyEvent::class.java,
                        Int::class.javaPrimitiveType!!,
                        object : Hooker() {
                            override fun beforeHookedMethod(param: HookParam) {
                                capturePolicyServices(param.thisObject)
                                val event = param.args.firstOrNull() as? KeyEvent ?: return
                                if (isReplayedVolumeEvent(event)) return
                                if (isVolumeKey(event) && MorseSequenceRuntimeState.active &&
                                    SettingsReader.load().operationMode == OperationMode.MORSE
                                ) return

                                val replay = trackCursorChord(event)
                                if (replay != null) injectReplayedVolumeEvent(replay)
                                if (handleSystemVolumeKey(event)) {
                                    param.setResult(0)
                                }
                            }
                        }
                    )
                    true
                }.onXposedFailure("hook $className volume-key cursor path").getOrDefault(false)
            ) {
                XposedBridge.log("$TAG: hooked $className volume-key cursor path")
                hookPolicyLifecycle(className, lpparam)
                return
            }
        }
        XposedBridge.log("$TAG: PhoneWindowManager volume hook unavailable")
    }

    private fun hookWindowManager(lpparam: PackageHookParam) {
        val windowManagerClass = XposedHelpers.findClassIfExists(
            "com.android.server.wm.WindowManagerService",
            lpparam.classLoader
        ) ?: run {
            if (!windowManagerClassUnavailableLogged) {
                windowManagerClassUnavailableLogged = true
                XposedBridge.log("$TAG: WindowManagerService class not found in ${lpparam.classLoader}")
            }
            return
        }
        XposedBridge.log("$TAG: found WindowManagerService class ${windowManagerClass.name}")
        runCatching {
            XposedHelpers.hookAllConstructors(windowManagerClass, object : Hooker() {
                override fun afterHookedMethod(param: HookParam) {
                    windowManagerService = param.thisObject
                    XposedBridge.log("$TAG: captured WindowManagerService instance")
                }
            })
        }.onXposedFailure("hook WindowManagerService lifecycle")
    }

    private fun hookPolicyLifecycle(
        className: String,
        lpparam: PackageHookParam
    ) {
        val policyClass = XposedHelpers.findClassIfExists(className, lpparam.classLoader) ?: return
        runCatching {
            XposedHelpers.hookAllMethods(policyClass, "initKeyCombinationRules", object : Hooker() {
                override fun afterHookedMethod(param: HookParam) {
                    capturePolicyServices(param.thisObject)
                }
            })
        }.onXposedFailure("hook $className policy lifecycle")
    }

    private fun hookImeVisibility(lpparam: PackageHookParam) {
        val imeClass = XposedHelpers.findClassIfExists(
            "com.android.server.inputmethod.InputMethodManagerService",
            lpparam.classLoader
        ) ?: return
        runCatching {
            XposedHelpers.hookAllConstructors(imeClass, object : Hooker() {
                override fun afterHookedMethod(param: HookParam) {
                    inputMethodManagerService = param.thisObject
                }
            })
            XposedHelpers.hookAllMethods(imeClass, "setImeWindowStatusLocked", object : Hooker() {
                override fun afterHookedMethod(param: HookParam) {
                    inputMethodManagerService = param.thisObject
                    val visibility = param.args.firstOrNull { it is Int } as? Int ?: return
                    imeWindowVisible = visibility and IME_VISIBLE != 0
                }
            })
        }.onXposedFailure("hook InputMethodManagerService visibility")
    }

    private fun hookMediaVolumePaths(lpparam: PackageHookParam) {
        hookAllMethodsIfPresent(
            "android.media.session.MediaSessionLegacyHelper",
            lpparam.classLoader,
            "sendVolumeKeyEvent"
        ) { param ->
            if (param.args.size >= 3 && param.args[0] is KeyEvent && param.args[2] == true &&
                handleSystemVolumeKey(param.args[0] as KeyEvent)
            ) {
                param.setResult(null)
            }
        }
        hookAllMethodsIfPresent(
            "com.android.server.media.MediaSessionService\$SessionManagerImpl",
            lpparam.classLoader,
            "dispatchVolumeKeyEvent"
        ) { param ->
            if (param.args.size >= 6 && param.args[3] is KeyEvent && param.args[5] == true &&
                handleSystemVolumeKey(param.args[3] as KeyEvent)
            ) {
                param.setResult(null)
            }
        }
    }

    private fun hookAllMethodsIfPresent(
        className: String,
        classLoader: ClassLoader,
        methodName: String,
        before: (Hooker.HookParam) -> Unit
    ) {
        val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: return
        runCatching {
            XposedHelpers.hookAllMethods(clazz, methodName, object : Hooker() {
                override fun beforeHookedMethod(param: HookParam) = before(param)
            })
        }.onXposedFailure("hook $className.$methodName")
    }

    private fun capturePolicyServices(policy: Any) {
        context = runCatching {
            XposedHelpers.getObjectField(policy, "mContext") as? Context
        }.onXposedFailure("read policy context").getOrNull() ?: context
        policyHandler = runCatching {
            XposedHelpers.getObjectField(policy, "mHandler") as? Handler
        }.onXposedFailure("read policy handler").getOrNull() ?: policyHandler
        if (windowManagerService == null) {
            findWindowManagerService(policy)?.let {
                windowManagerService = it
                XposedBridge.log("$TAG: captured WindowManagerService from policy")
            }
        }
    }

    private fun trackCursorChord(event: KeyEvent): KeyEvent? {
        val keyCode = event.keyCode
        val action = event.action
        synchronized(cursorLock) {
            when {
                keyCode == KeyEvent.KEYCODE_POWER -> {
                    if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        powerKeyPressed = true
                        if (volumeUpPressed || volumeDownPressed) {
                            cursorChordActive = true
                            return takePendingCursorDownLocked()
                        }
                    } else if (action == KeyEvent.ACTION_UP) {
                        powerKeyPressed = false
                    }
                }
                keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        if (!volumeUpPressed && !volumeDownPressed) cursorChordActive = false
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) volumeUpPressed = true
                        else volumeDownPressed = true
                        if (powerKeyPressed || (volumeUpPressed && volumeDownPressed)) {
                            cursorChordActive = true
                            return takePendingCursorDownLocked()
                        }
                    } else if (action == KeyEvent.ACTION_UP) {
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) volumeUpPressed = false
                        else volumeDownPressed = false
                    }
                }
            }
        }
        return null
    }

    private fun handleSystemVolumeKey(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) return false
        if (MorseSequenceRuntimeState.active && SettingsReader.load().operationMode == OperationMode.MORSE) {
            return false
        }

        if (isSystemUiOverlayActive(displayIdOf(event))) {
            cancelCursorPressForOverlay(keyCode)
            return false
        }

        var pendingUp: KeyEvent? = null
        var pendingUpMode: CursorControlMode? = null
        var currentHandlingMode: CursorControlMode? = null
        var injectCurrent = false
        var skipCurrentRelease = false
        synchronized(cursorLock) {
            val handlingPress = pressForKeyLocked(keyCode)
            val handlingMode = handlingPress?.mode
            currentHandlingMode = handlingMode
            if (cursorChordActive) {
                if (event.action == KeyEvent.ACTION_UP && handlingMode != null) cancelPressLocked(keyCode)
                if (event.action == KeyEvent.ACTION_UP && !volumeUpPressed && !volumeDownPressed) cursorChordActive = false
                return false
            }

            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val settings = SettingsReader.load()
                val mode = currentMode(settings)
                if (!isCursorControlAvailable(mode, displayIdOf(event))) return false
                setPressForKeyLocked(
                    keyCode,
                    CursorPress(
                        keyCode = keyCode,
                        mode = mode,
                        longPressAction = settings.cursorLongPressAction,
                        longPressMs = settings.cursorLongPressMs,
                        repeatIntervalMs = settings.cursorRepeatIntervalMs,
                        downEvent = KeyEvent(event)
                    )
                )
                val handler = policyHandler ?: Handler(Looper.getMainLooper())
                scheduleCursorDownLocked(pressForKeyLocked(keyCode)!!, handler)
            } else if (handlingMode == null) {
                return false
            } else if (event.action == KeyEvent.ACTION_DOWN) {
                // Physical repeat events are replaced by the configured long-press behavior.
            } else if (event.action == KeyEvent.ACTION_UP) {
                pendingUp = takePendingCursorDownLocked(keyCode)
                pendingUpMode = handlingMode
                val initialDispatched = handlingPress?.initialCursorDownDispatched == true
                skipCurrentRelease = handlingPress?.initialCursorReleased == true
                cancelPressLocked(keyCode)
                injectCurrent = pendingUp != null || (initialDispatched && !skipCurrentRelease)
                if (!volumeUpPressed && !volumeDownPressed) cursorChordActive = false
            }
        }
        if (pendingUp != null && pendingUpMode != null) {
            injectCursorPulse(pendingUp!!, pendingUpMode!!)
        } else if (injectCurrent) {
            injectCursorKeyEvent(event, currentHandlingMode ?: currentMode())
        }
        return true
    }

    private fun isVolumeKey(event: KeyEvent): Boolean =
        event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN

    private fun cancelCursorPressForOverlay(keyCode: Int) {
        synchronized(cursorLock) {
            cancelPressLocked(keyCode)
            if (pendingCursorDown?.keyCode == keyCode) {
                pendingCursorGeneration++
                pendingCursorDown = null
            }
        }
    }

    private fun currentMode(settings: AppSettings = SettingsReader.load()): CursorControlMode {
        return if (settings.enabled) settings.cursorControlMode else CursorControlMode.DISABLED
    }

    private fun isCursorControlAvailable(mode: CursorControlMode, displayId: Int = 0): Boolean {
        if (mode == CursorControlMode.DISABLED) return false
        val currentContext = context ?: resolveSystemContext()?.also { context = it } ?: return false
        val power = currentContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (power != null && !power.isInteractive) return false
        val telephony = currentContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        if (telephony != null && runCatching { telephony.callState }
                .onXposedFailure("read telephony call state")
                .getOrDefault(TelephonyManager.CALL_STATE_IDLE) != TelephonyManager.CALL_STATE_IDLE) return false
        val audio = currentContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audio?.mode == AudioManager.MODE_IN_CALL || audio?.mode == AudioManager.MODE_IN_COMMUNICATION) return false
        val keyguard = currentContext.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguard?.isKeyguardLocked == true) return false
        val systemUiOverlay = isSystemUiOverlayActive(displayId)
        val imeVisible = isImeVisible()
        XposedBridge.log(
            "$TAG: cursor availability mode=$mode display=$displayId " +
                "systemUiOverlay=$systemUiOverlay imeVisible=$imeVisible " +
                "wms=${windowManagerService != null}"
        )
        if (systemUiOverlay) return false
        return imeVisible
    }

    /** Reads WMS focus directly; IME visibility remains true while NotificationShade is open. */
    private fun isSystemUiOverlayActive(displayId: Int): Boolean {
        val service = windowManagerService ?: run {
            if (!windowManagerUnavailableLogged) {
                windowManagerUnavailableLogged = true
                XposedBridge.log("$TAG: WindowManagerService instance unavailable")
            }
            return false
        }
        return runCatching {
            val root = getObjectField(service, "mRoot") ?: run {
                if (!focusedWindowReadErrorLogged) {
                    focusedWindowReadErrorLogged = true
                    XposedBridge.log("$TAG: WindowManagerService.mRoot unavailable")
                }
                return@runCatching false
            }
            val display = callObjectMethod(root, "getDisplayContent", displayId)
                ?: callObjectMethod(root, "getTopFocusedDisplayContent")
            val focusedWindow = listOfNotNull(display, root, service)
                .firstNotNullOfOrNull { target ->
                    getObjectField(target, "mCurrentFocus")
                        ?: getObjectField(target, "mFocusedWindow")
                        ?: callObjectMethod(target, "getCurrentFocus")
                        ?: callObjectMethod(target, "getFocusedWindow")
                } ?: run {
                    if (!focusedWindowReadErrorLogged) {
                        focusedWindowReadErrorLogged = true
                        XposedBridge.log(
                            "$TAG: current focused window unavailable " +
                                "root=${root.javaClass.name} display=${display?.javaClass?.name}"
                        )
                    }
                    return@runCatching false
                }
            val attrs = getObjectField(focusedWindow, "mAttrs") ?: run {
                if (!focusedWindowReadErrorLogged) {
                    focusedWindowReadErrorLogged = true
                    XposedBridge.log("$TAG: focused window attrs unavailable")
                }
                return@runCatching false
            }
            val packageName = runCatching { getObjectField(attrs, "packageName") as? String }.getOrNull()
            if (packageName != "com.android.systemui" && packageName != "com.oplus.systemui") {
                return@runCatching false
            }
            val type = runCatching { XposedHelpers.getIntField(attrs, "type") }.getOrDefault(-1)
            val title = callObjectMethod(attrs, "getTitle")?.toString().orEmpty()
            val description = "$packageName type=$type title=$title"
            if (description != lastFocusedWindowDescription) {
                lastFocusedWindowDescription = description
                XposedBridge.log("$TAG: focused window $description")
            }
            type in SYSTEM_UI_PANEL_TYPES || title.contains("NotificationShade", ignoreCase = true) ||
                title.contains("ControlCenter", ignoreCase = true) || title.contains("Shade", ignoreCase = true)
        }.onFailure {
            if (!focusedWindowReadErrorLogged) {
                focusedWindowReadErrorLogged = true
                XposedBridge.log("$TAG: failed to read focused window: ${it.message}")
            }
        }.getOrDefault(false)
    }

    private fun findWindowManagerService(policy: Any): Any? {
        val directFields = listOf("mWindowManager", "mWindowManagerService", "mWindowManagerFuncs")
        for (fieldName in directFields) {
            val candidate = runCatching { getObjectField(policy, fieldName) }.getOrNull() ?: continue
            findWindowManagerService(candidate, 2)?.let { return it }
        }
        return null
    }

    private fun findWindowManagerService(candidate: Any, depth: Int): Any? {
        if (candidate.javaClass.name == "com.android.server.wm.WindowManagerService") return candidate
        if (depth == 0) return null
        val nested = runCatching {
            generateSequence(candidate.javaClass) { it.superclass }
                .flatMap { it.declaredFields.asSequence() }
                .filter { field ->
                    field.name.contains("windowManager", ignoreCase = true) ||
                        field.type.name.contains("WindowManager", ignoreCase = true)
                }
                .firstNotNullOfOrNull { field ->
                    field.isAccessible = true
                    field.get(candidate)?.let { findWindowManagerService(it, depth - 1) }
                }
        }.getOrNull()
        if (nested != null) return nested
        return null
    }

    private fun getObjectField(target: Any, fieldName: String): Any? = runCatching {
        XposedHelpers.getObjectField(target, fieldName)
    }.getOrNull()

    private fun callObjectMethod(target: Any, methodName: String, vararg args: Any?): Any? = runCatching {
        XposedHelpers.callMethod(target, methodName, *args).takeUnless { it === Unit }
    }.getOrNull()

    private fun isImeVisible(): Boolean {
        val service = inputMethodManagerService ?: return imeWindowVisible
        return runCatching {
            val userId = XposedHelpers.getIntField(service, "mCurrentImeUserId")
            val userData = XposedHelpers.callMethod(service, "getUserData", userId)
            val binding = XposedHelpers.getObjectField(userData, "mBindingController")
            val visibility = XposedHelpers.callMethod(binding, "getImeWindowVis") as Int
            imeWindowVisible = visibility and IME_VISIBLE != 0
            imeWindowVisible
        }.onXposedFailure("read current IME visibility").getOrElse {
            if (!imeVisibilityReadErrorLogged) {
                imeVisibilityReadErrorLogged = true
                XposedBridge.log("$TAG: failed to read current IME visibility: ${it.message}")
            }
            imeWindowVisible
        }
    }

    private fun scheduleCursorDownLocked(press: CursorPress, handler: Handler) {
        press.handler = handler
        val generation = ++pendingCursorGeneration
        pendingCursorDown = KeyEvent(press.downEvent)
        handler.postDelayed({ dispatchPendingCursorDown(generation) }, CURSOR_CHORD_DELAY_MS)

        val longRunnable = Runnable { triggerLongPress(press, handler) }
        press.longRunnable = longRunnable
        handler.postDelayed(longRunnable, press.longPressMs)
    }

    private fun dispatchPendingCursorDown(generation: Int) {
        val event: KeyEvent
        val mode: CursorControlMode
        synchronized(cursorLock) {
            val pending = pendingCursorDown ?: return
            val press = pressForKeyLocked(pending.keyCode) ?: return
            mode = press.mode
            if (generation != pendingCursorGeneration || cursorChordActive) return
            event = pending
            pendingCursorDown = null
            press.initialCursorDownDispatched = true
            press.initialCursorReleased = true
        }
        injectCursorPulse(event, mode)
    }

    private fun triggerLongPress(press: CursorPress, handler: Handler) {
        var releaseInitial: KeyEvent? = null
        var moveToEdge = false
        synchronized(cursorLock) {
            if (pressForKeyLocked(press.keyCode) !== press || press.longTriggered) return
            if (pendingCursorDown?.downTime == press.downEvent.downTime) {
                pendingCursorGeneration++
                pendingCursorDown = null
            }
            press.longTriggered = true
            if (press.initialCursorDownDispatched) {
                press.initialCursorReleased = true
                releaseInitial = press.downEvent
            }
            when (press.longPressAction) {
                CursorLongPressAction.NONE -> Unit
                CursorLongPressAction.REPEAT -> scheduleCursorRepeatLocked(press, handler)
                CursorLongPressAction.EDGE -> moveToEdge = true
            }
        }
        releaseInitial?.let { injectCursorKeyEvent(it, press.mode, KeyEvent.ACTION_UP, 0) }
        if (moveToEdge) injectCursorEdge(press.downEvent, press.mode)
    }

    private fun scheduleCursorRepeatLocked(press: CursorPress, handler: Handler) {
        val repeatRunnable = object : Runnable {
            override fun run() {
                val shouldRepeat = synchronized(cursorLock) {
                    pressForKeyLocked(press.keyCode) === press && press.longTriggered &&
                        press.longPressAction == CursorLongPressAction.REPEAT
                }
                if (!shouldRepeat) return
                injectCursorPulse(press.downEvent, press.mode)
                synchronized(cursorLock) {
                    if (pressForKeyLocked(press.keyCode) === press && press.longTriggered) {
                        handler.postDelayed(this, press.repeatIntervalMs)
                    }
                }
            }
        }
        press.repeatRunnable = repeatRunnable
        handler.postDelayed(repeatRunnable, press.repeatIntervalMs)
    }

    private fun takePendingCursorDownLocked(keyCode: Int): KeyEvent? {
        if (pendingCursorDown?.keyCode != keyCode) return null
        return takePendingCursorDownLocked()
    }

    private fun takePendingCursorDownLocked(): KeyEvent? {
        val event = pendingCursorDown
        pendingCursorGeneration++
        pendingCursorDown = null
        event?.let { cancelPressLocked(it.keyCode) }
        return event
    }

    private fun pressForKeyLocked(keyCode: Int): CursorPress? =
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) volumeUpPress else volumeDownPress

    private fun setPressForKeyLocked(keyCode: Int, press: CursorPress?) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) volumeUpPress = press else volumeDownPress = press
    }

    private fun cancelPressLocked(keyCode: Int) {
        val press = pressForKeyLocked(keyCode) ?: return
        press.longRunnable?.let { press.handler?.removeCallbacks(it) }
        press.repeatRunnable?.let { press.handler?.removeCallbacks(it) }
        setPressForKeyLocked(keyCode, null)
    }

    private fun isReplayedVolumeEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        return (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) &&
            event.flags and KeyEvent.FLAG_EDITOR_ACTION != 0
    }

    private fun injectReplayedVolumeEvent(volumeEvent: KeyEvent) {
        val currentContext = context ?: resolveSystemContext()?.also { context = it } ?: return
        val replay = KeyEvent(
            volumeEvent.downTime,
            android.os.SystemClock.uptimeMillis(),
            volumeEvent.action,
            volumeEvent.keyCode,
            volumeEvent.repeatCount,
            volumeEvent.metaState,
            KeyCharacterMap.VIRTUAL_KEYBOARD,
            volumeEvent.scanCode,
            volumeEvent.flags or KeyEvent.FLAG_FROM_SYSTEM or KeyEvent.FLAG_EDITOR_ACTION,
            volumeEvent.source
        )
        copyDisplayId(replay, volumeEvent)
        injectInputEvent(currentContext, replay, "replay volume key")
    }

    private fun injectCursorPulse(volumeEvent: KeyEvent, mode: CursorControlMode) {
        injectCursorKeyEvent(volumeEvent, mode, KeyEvent.ACTION_DOWN, 1)
        injectCursorKeyEvent(volumeEvent, mode, KeyEvent.ACTION_UP, 1)
    }

    private fun injectCursorEdge(volumeEvent: KeyEvent, mode: CursorControlMode) {
        val moveLeft = if (volumeEvent.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mode == CursorControlMode.VOLUME_UP_LEFT
        } else {
            mode == CursorControlMode.VOLUME_UP_RIGHT
        }
        val edgeCode = if (moveLeft) KeyEvent.KEYCODE_MOVE_HOME else KeyEvent.KEYCODE_MOVE_END
        injectCursorKeyCode(volumeEvent, edgeCode, KeyEvent.ACTION_DOWN, 0, "cursor edge")
        injectCursorKeyCode(volumeEvent, edgeCode, KeyEvent.ACTION_UP, 0, "cursor edge")
    }

    private fun injectCursorKeyEvent(
        volumeEvent: KeyEvent,
        mode: CursorControlMode,
        action: Int = volumeEvent.action,
        repeatCount: Int = volumeEvent.repeatCount
    ) {
        if (mode == CursorControlMode.DISABLED) return
        val leftOnUp = mode == CursorControlMode.VOLUME_UP_LEFT
        val cursorCode = if (volumeEvent.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (leftOnUp) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        } else {
            if (leftOnUp) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
        }
        injectCursorKeyCode(volumeEvent, cursorCode, action, repeatCount, "cursor key")
    }

    private fun injectCursorKeyCode(
        volumeEvent: KeyEvent,
        cursorCode: Int,
        action: Int,
        repeatCount: Int,
        description: String
    ) {
        if (isSystemUiOverlayActive(displayIdOf(volumeEvent))) {
            cancelCursorPressForOverlay(volumeEvent.keyCode)
            return
        }
        val cursorEvent = KeyEvent(
            volumeEvent.downTime,
            android.os.SystemClock.uptimeMillis(),
            action,
            cursorCode,
            repeatCount,
            volumeEvent.metaState,
            KeyCharacterMap.VIRTUAL_KEYBOARD,
            0,
            volumeEvent.flags or KeyEvent.FLAG_FROM_SYSTEM or KeyEvent.FLAG_VIRTUAL_HARD_KEY,
            InputDevice.SOURCE_KEYBOARD
        )
        copyDisplayId(cursorEvent, volumeEvent)
        injectInputEvent(
            context ?: resolveSystemContext()?.also { context = it } ?: return,
            cursorEvent,
            description
        )
    }

    private fun resolveSystemContext(): Context? = runCatching {
        val threadClass = Class.forName("android.app.ActivityThread")
        (threadClass.getMethod("currentApplication").invoke(null) as? Context)
            ?: run {
                val thread = threadClass.getMethod("currentActivityThread").invoke(null)
                threadClass.getMethod("getSystemContext").invoke(thread) as? Context
            }
    }.onXposedFailure("resolve system context").getOrNull()

    private fun copyDisplayId(target: KeyEvent, source: KeyEvent) {
        runCatching {
            XposedHelpers.callMethod(target, "setDisplayId", XposedHelpers.callMethod(source, "getDisplayId"))
        }.onXposedFailure("copy display ID")
    }

    private fun displayIdOf(event: KeyEvent): Int = runCatching {
        XposedHelpers.callMethod(event, "getDisplayId") as Int
    }.getOrDefault(0)

    private fun injectInputEvent(currentContext: Context, event: KeyEvent, description: String) {
        runCatching {
            val inputManager = currentContext.getSystemService(Context.INPUT_SERVICE)
            XposedHelpers.callMethod(inputManager, "injectInputEvent", event, 0)
        }.onXposedFailure("inject $description")
    }
}
