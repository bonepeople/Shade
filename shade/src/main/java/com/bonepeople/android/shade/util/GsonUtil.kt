package com.bonepeople.android.shade.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonUtil {
    val gson: Gson by lazy { GsonBuilder().disableHtmlEscaping().create() }

    fun toJson(data: Any?): String {
        return gson.toJson(data)
    }

    inline fun <reified R> toObject(json: String?): R {
        return gson.fromJson(json, R::class.java) ?: throw IllegalStateException("Empty JSON string for decode '$json'")
    }
}