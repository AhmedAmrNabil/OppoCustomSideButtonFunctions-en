package com.slimenull.customsidebuttonfunctions.model

import androidx.annotation.StringRes
import com.slimenull.customsidebuttonfunctions.R

/** Direction used when physical volume keys are repurposed as text-cursor keys. */
enum class CursorControlMode(@get:StringRes val titleRes: Int, val defaultTitle: String) {
    DISABLED(R.string.cursor_control_disabled, "关闭"),
    VOLUME_UP_LEFT(R.string.cursor_control_vol_up_left, "上键左移"),
    VOLUME_UP_RIGHT(R.string.cursor_control_vol_up_right, "上键右移");

    val title: String get() = defaultTitle
}
