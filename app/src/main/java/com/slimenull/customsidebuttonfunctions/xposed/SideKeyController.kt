package com.slimenull.customsidebuttonfunctions.xposed

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import com.slimenull.customsidebuttonfunctions.model.ActionType
import com.slimenull.customsidebuttonfunctions.model.AppSettings
import com.slimenull.customsidebuttonfunctions.model.CustomActionSettings
import com.slimenull.customsidebuttonfunctions.model.OperationMode
import com.slimenull.customsidebuttonfunctions.model.SideKeyCombinationAction

/** Published only inside system_server so the cursor hook can yield Morse volume keys. */
internal object MorseSequenceRuntimeState {
    @Volatile
    var active: Boolean = false
}

/** Thread-safe gesture state machine shared by framework and raw input sources. */
internal class SideKeyController {
    private val handler = Handler(Looper.getMainLooper())
    private val lock = Any()
    private var pressed = false
    private var longTriggered = false
    private var longPressReached = false
    private var secondClick = false
    private var pendingSingle = false
    private var combinationTriggered = false
    private val combinationKeysDown = mutableSetOf<Int>()
    private var activeSettings = AppSettings()
    private var activeInteractive = true
    private var longRunnable: Runnable? = null
    private var singleRunnable: Runnable? = null
    private val morseSequence = StringBuilder()
    private var morseDownAt = 0L
    private var morseLastUpAt = 0L
    private var morseLongReached = false
    private var morseTriggeredWhilePressed = false
    private var morseThresholdRunnable: Runnable? = null
    private var morseFinishRunnable: Runnable? = null
    private val morseAuxiliaryDownAt = mutableMapOf<Int, Long>()
    private val morseAuxiliaryLongReached = mutableSetOf<Int>()
    private val morseAuxiliaryLongRunnables = mutableMapOf<Int, Runnable>()

    fun onKeyEvent(event: KeyEvent, executor: ActionExecutor) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> onDown(SettingsReader.load(), executor, true)
            KeyEvent.ACTION_UP -> onUp(SettingsReader.load(), executor, true)
        }
    }

    fun cancel() {
        synchronized(lock) {
            pressed = false
            longTriggered = false
            longPressReached = false
            resetClickState()
            longRunnable?.let(handler::removeCallbacks)
            longRunnable = null
            combinationTriggered = false
            combinationKeysDown.clear()
            resetMorseState()
        }
    }

    /** Handles volume/power events while the side key is held for a configured combination. */
    fun onAuxiliaryKeyEvent(event: KeyEvent, executor: ActionExecutor): Boolean {
        synchronized(lock) {
            val settings = SettingsReader.load()
            if (!settings.enabled || settings.operationMode == OperationMode.DISABLED) return false
            val action = when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> activeSettings.sideVolumeUpAction
                KeyEvent.KEYCODE_VOLUME_DOWN -> activeSettings.sideVolumeDownAction
                KeyEvent.KEYCODE_POWER -> activeSettings.sidePowerAction
                else -> SideKeyCombinationAction.NONE
            }
            if (pressed && activeSettings.hasConfiguredCombination && action != SideKeyCombinationAction.NONE) {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        if (!combinationKeysDown.add(event.keyCode)) return true
                        combinationTriggered = true
                        longRunnable?.let(handler::removeCallbacks)
                        longRunnable = null
                        resetClickState()
                        resetMorseState()
                        executor.executeCombination(action, activeSettings.combinationVibrationEnabled)
                        return true
                    }
                    KeyEvent.ACTION_UP -> {
                        if (combinationKeysDown.remove(event.keyCode)) return true
                    }
                }
                return false
            }
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (settings.operationMode == OperationMode.MORSE && handleMorseAuxiliaryKey(event, executor, settings)) {
                    return true
                }
            }
            return false
        }
    }

    /**
     * Adds a volume-key short/long symbol to an already started Morse sequence.
     * A volume key is deliberately ignored while the sequence is empty so a
     * volume press can still perform its normal system volume operation.
     */
    private fun handleMorseAuxiliaryKey(
        event: KeyEvent,
        executor: ActionExecutor,
        settings: AppSettings
    ): Boolean {
        val now = SystemClock.uptimeMillis()
        if (morseSequence.isEmpty()) return false
        if (now - morseLastUpAt >= settings.morseCommandWindowMs && morseAuxiliaryDownAt.isEmpty()) {
            finishMorseSequence(executor, settings)
            return false
        }
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            if (morseAuxiliaryDownAt.containsKey(event.keyCode)) return true
            morseFinishRunnable?.let(handler::removeCallbacks)
            morseFinishRunnable = null
            morseAuxiliaryDownAt[event.keyCode] = now
            morseAuxiliaryLongReached.remove(event.keyCode)
            val runnable = Runnable {
                synchronized(lock) {
                    if (morseAuxiliaryDownAt[event.keyCode] == now &&
                        morseAuxiliaryLongReached.add(event.keyCode) &&
                        settings.morseLongVibrationEnabled
                    ) {
                        executor.vibrateInstantCue()
                    }
                }
            }
            morseAuxiliaryLongRunnables[event.keyCode] = runnable
            handler.postDelayed(runnable, settings.morseLongPressMs)
            return true
        }
        if (event.action != KeyEvent.ACTION_UP) return true
        val downAt = morseAuxiliaryDownAt.remove(event.keyCode) ?: return true
        morseAuxiliaryLongRunnables.remove(event.keyCode)?.let(handler::removeCallbacks)
        val longReached = morseAuxiliaryLongReached.remove(event.keyCode)
        val isLong = longReached || now - downAt >= settings.morseLongPressMs
        if (isLong && !longReached && settings.morseLongVibrationEnabled) {
            executor.vibrateInstantCue()
        }
        appendMorseSymbol(
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (isLong) 'U' else 'u'
            } else {
                if (isLong) 'D' else 'd'
            },
            executor,
            settings
        )
        return true
    }

    fun onDown(settings: AppSettings, executor: ActionExecutor, interactive: Boolean) {
        synchronized(lock) {
            if (!settings.enabled || settings.operationMode == OperationMode.DISABLED || pressed) return
            if (settings.operationMode == OperationMode.MORSE) {
                resetClickState()
                longRunnable?.let(handler::removeCallbacks)
                longRunnable = null
                onMorseDown(settings, executor, interactive)
                return
            }
            resetMorseState()
            pressed = true
            activeSettings = settings
            activeInteractive = interactive
            longTriggered = false
            longPressReached = false
            combinationTriggered = false
            combinationKeysDown.clear()
            secondClick = pendingSingle
            if (secondClick) {
                pendingSingle = false
                singleRunnable?.let(handler::removeCallbacks)
                singleRunnable = null
            }
            val runnable = Runnable {
                synchronized(lock) {
                    if (pressed && !longTriggered) {
                        val current = SettingsReader.load()
                        if (!current.enabled || current.operationMode != OperationMode.SIMPLE) {
                            cancel()
                            return@synchronized
                        }
                        if (activeSettings.hasConfiguredCombination) {
                            // Keep the side key pending so a later volume/power key can form a
                            // combination. If no combination arrives, execute on side-key up.
                            longPressReached = true
                            if (activeSettings.vibrationEnabled && activeSettings.longAction != ActionType.NONE) {
                                executor.vibrateInstantCue()
                            }
                        } else {
                            longTriggered = true
                            execute(
                                activeSettings.longAction,
                                activeSettings.longCustom,
                                activeSettings,
                            executor,
                            activeInteractive,
                            activeSettings.longWakeScreen,
                            activeSettings.vibrationEnabled || activeSettings.longVibrationEnabled
                            )
                        }
                    }
                }
            }
            longRunnable = runnable
            handler.postDelayed(runnable, settings.longPressMs)
        }
    }

    fun onUp(settings: AppSettings, executor: ActionExecutor, interactive: Boolean) {
        synchronized(lock) {
            if (!pressed) return
            if (!settings.enabled || settings.operationMode != activeSettings.operationMode) {
                cancel()
                return
            }
            if (combinationTriggered) {
                pressed = false
                combinationTriggered = false
                combinationKeysDown.clear()
                longPressReached = false
                longRunnable?.let(handler::removeCallbacks)
                longRunnable = null
                resetClickState()
                resetMorseState()
                return
            }
            if (activeSettings.operationMode == OperationMode.MORSE) {
                onMorseUp(executor, interactive)
                return
            }
            pressed = false
            longRunnable?.let(handler::removeCallbacks)
            longRunnable = null
            if (!longTriggered) activeInteractive = interactive
            if (longPressReached) {
                longPressReached = false
                resetClickState()
                execute(
                    activeSettings.longAction,
                    activeSettings.longCustom,
                    activeSettings,
                    executor,
                    activeInteractive,
                    activeSettings.longWakeScreen,
                    activeSettings.vibrationEnabled || activeSettings.longVibrationEnabled
                )
                return
            }
            if (longTriggered) {
                resetClickState()
                return
            }
            if (secondClick) {
                secondClick = false
                execute(
                    activeSettings.doubleAction,
                    activeSettings.doubleCustom,
                    activeSettings,
                    executor,
                    activeInteractive,
                    activeSettings.doubleWakeScreen,
                    activeSettings.vibrationEnabled || activeSettings.doubleVibrationEnabled
                )
                return
            }
            if (settings.doubleAction == ActionType.NONE) {
                execute(
                    activeSettings.singleAction,
                    activeSettings.singleCustom,
                    activeSettings,
                    executor,
                    activeInteractive,
                    activeSettings.singleWakeScreen,
                    activeSettings.vibrationEnabled || activeSettings.singleVibrationEnabled
                )
                return
            }
            pendingSingle = true
            val runnable = Runnable {
                synchronized(lock) {
                    if (pendingSingle) {
                        pendingSingle = false
                        val current = SettingsReader.load()
                        if (current.enabled && current.operationMode == OperationMode.SIMPLE) {
                            execute(
                                activeSettings.singleAction,
                                activeSettings.singleCustom,
                                activeSettings,
                                executor,
                                activeInteractive,
                                activeSettings.singleWakeScreen,
                                activeSettings.vibrationEnabled || activeSettings.singleVibrationEnabled
                            )
                        }
                    }
                }
            }
            singleRunnable = runnable
            handler.postDelayed(runnable, settings.doubleClickWindowMs)
        }
    }

    private fun resetClickState() {
        secondClick = false
        pendingSingle = false
        singleRunnable?.let(handler::removeCallbacks)
        singleRunnable = null
    }

    private fun onMorseDown(settings: AppSettings, executor: ActionExecutor, interactive: Boolean) {
        val now = SystemClock.uptimeMillis()
        if (morseSequence.isNotEmpty() && now - morseLastUpAt >= activeSettings.morseCommandWindowMs) {
            finishMorseSequence(executor)
        }
        morseFinishRunnable?.let(handler::removeCallbacks)
        morseFinishRunnable = null
        if (morseSequence.isEmpty()) {
            activeSettings = settings
            activeInteractive = interactive
        } else {
            activeInteractive = activeInteractive && interactive
        }
        pressed = true
        morseDownAt = now
        morseLongReached = false
        morseTriggeredWhilePressed = false
        if (activeSettings.morsePressVibrationEnabled) executor.vibrateInstantCue()

        val runnable = object : Runnable {
            override fun run() {
                synchronized(lock) {
                    if (morseThresholdRunnable === this && pressed && !morseLongReached) {
                        val current = SettingsReader.load()
                        if (!current.enabled || current.operationMode != OperationMode.MORSE) {
                            cancel()
                            return@synchronized
                        }
                        morseLongReached = true
                        val sequence = morseSequence.toString() + '1'
                        val binding = current.morseBindings.firstOrNull { it.sequence == sequence }
                        val hasLongerMatch = current.morseBindings.any {
                            it.sequence.length > sequence.length && it.sequence.startsWith(sequence)
                        }
                        if (current.morseImmediateExecutionEnabled && !current.hasConfiguredCombination &&
                            binding != null && !hasLongerMatch) {
                            morseTriggeredWhilePressed = true
                            morseSequence.clear()
                            MorseSequenceRuntimeState.active = false
                            morseFinishRunnable?.let(handler::removeCallbacks)
                            morseFinishRunnable = null
                            execute(
                                binding.action,
                                binding.custom,
                                current,
                                executor,
                                activeInteractive,
                                binding.wakeScreen,
                                current.vibrationEnabled || binding.vibrationEnabled
                            )
                            return@synchronized
                        }
                            if (activeSettings.morseLongVibrationEnabled) executor.vibrateInstantCue()
                    }
                }
            }
        }
        morseThresholdRunnable = runnable
        handler.postDelayed(runnable, activeSettings.morseLongPressMs)
    }

    private fun onMorseUp(executor: ActionExecutor, interactive: Boolean) {
        if (!pressed) return
        pressed = false
        morseThresholdRunnable?.let(handler::removeCallbacks)
        morseThresholdRunnable = null
        if (morseTriggeredWhilePressed) {
            morseTriggeredWhilePressed = false
            morseSequence.clear()
            MorseSequenceRuntimeState.active = false
            morseFinishRunnable?.let(handler::removeCallbacks)
            morseFinishRunnable = null
            morseLongReached = false
            return
        }
        val now = SystemClock.uptimeMillis()
        val isLong = morseLongReached || now - morseDownAt >= activeSettings.morseLongPressMs
        if (isLong && !morseLongReached && activeSettings.morseLongVibrationEnabled) {
            executor.vibrateInstantCue()
        }
        activeInteractive = activeInteractive && interactive
        appendMorseSymbol(if (isLong) '1' else '0', executor, SettingsReader.load())
    }

    private fun appendMorseSymbol(symbol: Char, executor: ActionExecutor, settings: AppSettings) {
        morseSequence.append(symbol)
        MorseSequenceRuntimeState.active = true
        morseLastUpAt = SystemClock.uptimeMillis()
        if (!settings.enabled || settings.operationMode != OperationMode.MORSE) {
            resetMorseState()
            return
        }
        val sequence = morseSequence.toString()
        val exactMatch = settings.morseBindings.any { it.sequence == sequence }
        val hasLongerMatch = settings.morseBindings.any {
            it.sequence.length > sequence.length && it.sequence.startsWith(sequence)
        }
        if (settings.morseImmediateExecutionEnabled && exactMatch && !hasLongerMatch &&
            !settings.hasConfiguredCombination
        ) {
            finishMorseSequence(executor, settings)
            return
        }
        val runnable = object : Runnable {
            override fun run() {
                synchronized(lock) {
                    if (morseFinishRunnable === this && !pressed && morseAuxiliaryDownAt.isEmpty()) {
                        finishMorseSequence(executor, settings)
                    }
                }
            }
        }
        morseFinishRunnable = runnable
        handler.postDelayed(runnable, activeSettings.morseCommandWindowMs)
    }

    private fun finishMorseSequence(executor: ActionExecutor, settings: AppSettings = SettingsReader.load()) {
        morseFinishRunnable?.let(handler::removeCallbacks)
        morseFinishRunnable = null
        val sequence = morseSequence.toString()
        morseSequence.clear()
        MorseSequenceRuntimeState.active = false
        if (sequence.isEmpty()) return
        if (!settings.enabled || settings.operationMode != OperationMode.MORSE) return
        val binding = settings.morseBindings.firstOrNull { it.sequence == sequence }
        if (binding == null) {
            executor.notifyUnknownMorseSequence(settings)
            return
        }
        execute(
            binding.action,
            binding.custom,
            settings,
            executor,
            activeInteractive,
            binding.wakeScreen,
            settings.vibrationEnabled || binding.vibrationEnabled
        )
    }

    private fun resetMorseState() {
        morseThresholdRunnable?.let(handler::removeCallbacks)
        morseThresholdRunnable = null
        morseFinishRunnable?.let(handler::removeCallbacks)
        morseFinishRunnable = null
        morseAuxiliaryLongRunnables.values.forEach(handler::removeCallbacks)
        morseAuxiliaryLongRunnables.clear()
        morseAuxiliaryDownAt.clear()
        morseAuxiliaryLongReached.clear()
        morseSequence.clear()
        MorseSequenceRuntimeState.active = false
        morseLongReached = false
        morseTriggeredWhilePressed = false
    }

    private fun execute(
        action: ActionType,
        custom: CustomActionSettings,
        settings: AppSettings,
        executor: ActionExecutor,
        interactive: Boolean,
        wakeScreen: Boolean,
        vibrationEnabled: Boolean
    ) {
        if (action != ActionType.NONE) {
            executor.execute(action, custom, settings, interactive, wakeScreen, vibrationEnabled)
        }
    }
}
