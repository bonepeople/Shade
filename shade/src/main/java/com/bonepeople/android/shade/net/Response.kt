package com.bonepeople.android.shade.net

import com.google.gson.annotations.SerializedName

class Response {
    @SerializedName("code")
    var code: Int = 0

    @SerializedName("msg")
    var msg: String = ""

    @SerializedName("data")
    var data: String = ""

    fun onSuccess(action: (value: String) -> Unit) = apply {
        kotlin.runCatching {
            if (code == SUCCESSFUL) {
                action(data)
            }
        }.onFailure {
            throw it
        }
    }

    fun onFailure(action: (code: Int, msg: String) -> Unit) = apply {
        if (code == CANCEL) return this
        kotlin.runCatching {
            if (code != SUCCESSFUL) {
                action(code, msg)
            }
        }.onFailure {
            throw it
        }
    }

    companion object {
        const val SUCCESSFUL = 1
        const val FAILURE = 0
        const val CANCEL = -1
    }
}