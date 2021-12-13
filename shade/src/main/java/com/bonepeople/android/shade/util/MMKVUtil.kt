package com.bonepeople.android.shade.util

import com.bonepeople.android.shade.Lighting
import com.tencent.mmkv.MMKV

object MMKVUtil {
    private val storage: MMKV? by lazy {
        MMKV.initialize(Lighting.appInformation.getApplication())
        MMKV.defaultMMKV()
    }

    @JvmStatic
    fun putString(key: String, value: String) {
        storage?.putString(key, value)
    }

    @JvmStatic
    @JvmOverloads
    fun getString(key: String, default: String = ""): String {
        return storage?.getString(key, default) ?: default
    }

    @JvmStatic
    fun putBoolean(key: String, value: Boolean) {
        storage?.putBoolean(key, value)
    }

    @JvmStatic
    @JvmOverloads
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return storage?.getBoolean(key, default) ?: default
    }
}