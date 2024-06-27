package androidx.shade.net

import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("code")
    var code: Int = 0,
    @SerializedName("msg")
    var msg: String = "",
    @SerializedName("data")
    var data: String = "",
) {
    suspend fun onSuccess(action: suspend (value: String) -> Unit) = apply {
        kotlin.runCatching {
            if (code == SUCCESSFUL) {
                action(data)
            }
        }.onFailure {
            code = FAILURE
            msg = it.message.toString()
        }
    }

    suspend fun onFailure(action: suspend (code: Int, msg: String) -> Unit) = apply {
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