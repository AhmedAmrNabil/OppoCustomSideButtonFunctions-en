package com.slimenull.customsidebuttonfunctions.model

import androidx.annotation.StringRes
import com.slimenull.customsidebuttonfunctions.R

enum class OperationMode(@get:StringRes val titleRes: Int, val defaultTitle: String) {
    DISABLED(R.string.operation_mode_disabled, "关闭"),
    SIMPLE(R.string.operation_mode_simple, "简单"),
    MORSE(R.string.operation_mode_morse, "摩斯电码");

    val title: String get() = defaultTitle
}
