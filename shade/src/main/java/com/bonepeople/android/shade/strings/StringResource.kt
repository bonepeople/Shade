package com.bonepeople.android.shade.strings

import java.util.Locale

/**
 * 多语言文字工具类
 */
internal object StringResource {
    private val localStrings = LinkedHashMap<String, LinkedHashMap<String, AppString>>()

    /**
     * 注册字符串实例
     * + 以先后顺序关系确定默认展示的字符串，在未找到匹配的字符串时优先展示先注册的字符串
     */
    fun registerAppString(locale: Locale, strings: AppString) {
        val language: LinkedHashMap<String, AppString> = localStrings.getOrPut(locale.language) { LinkedHashMap() }
        language[locale.country] = strings
    }

    /**
     * 获取当前语言的字符串实例
     */
    fun getAppString(): AppString {
        require(localStrings.isNotEmpty()) { "未注册任何语言，请注册后再使用" }
        val locale: Locale = Locale.getDefault()
        val language: LinkedHashMap<String, AppString> = localStrings.getOrPut(locale.language) { LinkedHashMap() }
        val strings: AppString? = language[locale.country]

        return strings ?: if (language.isEmpty()) { //语言维度未找到，返回注册的第一个语言文字
            localStrings.values.first().values.first()
        } else { //国家维度未找到，返回同语言字符串
            language.values.first()
        }
    }
}