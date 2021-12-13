package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

class HttpResponse<R> {
    @SerializedName("code")
    var code: Int = 0

    @SerializedName("msg")
    var msg: String = ""

    @SerializedName("data")
    var data: R? = null

    private fun isSuccessful(): Boolean {
        return code == SUCCESSFUL && data != null
    }

    fun onSuccess(action: (value: R) -> Unit): HttpResponse<R> {
        kotlin.runCatching {
            if (isSuccessful()) {
                action(data!!)
            }
        }.onFailure {
            throw it
        }
        return this
    }

    fun onFailure(action: (code: Int, msg: String) -> Unit): HttpResponse<R> {
        if (code == CANCEL) return this
        kotlin.runCatching {
            if (!isSuccessful()) {
                action(code, msg)
            }
        }.onFailure {
            throw it
        }
        return this
    }

    companion object {
        const val SUCCESSFUL = 1
        const val FAILURE = 0
        const val CANCEL = -1
    }
}