package com.slimenull.customsidebuttonfunctions.model

import androidx.annotation.StringRes
import com.slimenull.customsidebuttonfunctions.R

enum class SideKeyCombinationAction(@get:StringRes val titleRes: Int, val defaultTitle: String) {
    NONE(R.string.side_key_combo_none, "不使用"),
    MEDIA_PREVIOUS(R.string.side_key_combo_media_prev, "媒体切换上一首"),
    MEDIA_NEXT(R.string.side_key_combo_media_next, "媒体切换下一首"),
    MEDIA_PAUSE(R.string.side_key_combo_media_pause, "媒体暂停/播放");

    val title: String get() = defaultTitle
}
