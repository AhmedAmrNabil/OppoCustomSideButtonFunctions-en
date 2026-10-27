package com.slimenull.customsidebuttonfunctions.model

import androidx.annotation.StringRes
import com.slimenull.customsidebuttonfunctions.R

enum class CommonAction(@get:StringRes val titleRes: Int, val defaultTitle: String) {
    WECHAT_PAY(R.string.common_action_wechat_pay, "微信付款码"),
    WECHAT_SCAN(R.string.common_action_wechat_scan, "微信扫一扫"),
    ALIPAY_PAY(R.string.common_action_alipay_pay, "支付宝付款码"),
    ALIPAY_RECEIVE(R.string.common_action_alipay_receive, "支付宝收款码"),
    ALIPAY_SCAN(R.string.common_action_alipay_scan, "支付宝扫一扫"),
    FLASH_MEMORY(R.string.common_action_flash_memo, "一键闪记"),
    XIAOBU_MEMORY(R.string.common_action_xiaobu_memory, "小布记忆");

    val title: String get() = defaultTitle
}
