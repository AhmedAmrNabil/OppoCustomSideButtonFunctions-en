package com.slimenull.customsidebuttonfunctions.model

import androidx.annotation.StringRes
import com.slimenull.customsidebuttonfunctions.R

/** Actions exposed to every gesture. KEEP enum names stable: they are persisted in SharedPreferences. */
enum class ActionType(
    @get:StringRes val titleRes: Int,
    @get:StringRes val descriptionRes: Int,
    val defaultTitle: String
) {
    NONE(R.string.action_none_title, R.string.action_none_desc, "不执行操作"),
    SHOW_RINGER(R.string.action_show_ringer_title, R.string.action_show_ringer_desc, "显示响铃 / 振动 / 静音状态"),
    CYCLE_RINGER(R.string.action_cycle_ringer_title, R.string.action_cycle_ringer_desc, "切换响铃 / 振动 / 静音"),
    MEDIA_PREVIOUS(R.string.action_media_prev_title, R.string.action_media_prev_desc, "媒体切换上一首"),
    MEDIA_NEXT(R.string.action_media_next_title, R.string.action_media_next_desc, "媒体切换下一首"),
    MEDIA_PLAY_PAUSE(R.string.action_media_play_pause_title, R.string.action_media_play_pause_desc, "媒体暂停/播放"),
    TOGGLE_DND(R.string.action_toggle_dnd_title, R.string.action_toggle_dnd_desc, "开启或关闭免打扰"),
    CAMERA(R.string.action_camera_title, R.string.action_camera_desc, "打开相机"),
    FLASHLIGHT(R.string.action_flashlight_title, R.string.action_flashlight_desc, "切换手电筒"),
    RECORDING(R.string.action_recording_title, R.string.action_recording_desc, "开始 / 结束录音"),
    SCREENSHOT(R.string.action_screenshot_title, R.string.action_screenshot_desc, "系统截屏"),
    COMMON_FUNCTION(R.string.action_common_function_title, R.string.action_common_function_desc, "常用功能"),
    XIAOBU_SHORTCUT(R.string.action_xiaobu_shortcut_title, R.string.action_xiaobu_shortcut_desc, "执行小布快捷指令"),
    CUSTOM_ACTIVITY(R.string.action_custom_activity_title, R.string.action_custom_activity_desc, "自定义 Activity"),
    CUSTOM_URL(R.string.action_custom_url_title, R.string.action_custom_url_desc, "自定义 Url Scheme"),
    SHELL_COMMAND(R.string.action_shell_command_title, R.string.action_shell_command_desc, "执行 Shell 指令");

    val title: String get() = defaultTitle
    val description: String get() = defaultTitle
}
