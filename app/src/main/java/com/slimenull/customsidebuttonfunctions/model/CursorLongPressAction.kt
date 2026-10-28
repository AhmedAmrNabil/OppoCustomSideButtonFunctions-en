package com.slimenull.customsidebuttonfunctions.model

import androidx.annotation.StringRes
import com.slimenull.customsidebuttonfunctions.R

enum class CursorLongPressAction(@get:StringRes val titleRes: Int, val defaultTitle: String) {
    NONE(R.string.cursor_long_press_none, "不执行任何操作"),
    REPEAT(R.string.cursor_long_press_repeat, "连续移动"),
    EDGE(R.string.cursor_long_press_edge, "移动到首尾");

    val title: String get() = defaultTitle
}

const val DEFAULT_CURSOR_LONG_PRESS_MS = 500L
const val DEFAULT_CURSOR_REPEAT_INTERVAL_MS = 32L
const val MIN_CURSOR_LONG_PRESS_MS = 100L
const val MAX_CURSOR_LONG_PRESS_MS = 1_000L
const val MIN_CURSOR_REPEAT_INTERVAL_MS = 16L
const val MAX_CURSOR_REPEAT_INTERVAL_MS = 200L
