package androidx.shade.data

import com.google.gson.annotations.SerializedName

internal class LogRequest {
    @SerializedName("appName")
    var appName = ""

    @SerializedName("userId")
    var userId = ""

    @SerializedName("androidId")
    var androidId = ""

    @SerializedName("type")
    var type = ""

    @SerializedName("code")
    var code = 0

    @SerializedName("name")
    var name = ""

    @SerializedName("message")
    var message = ""

    @SerializedName("time")
    var time = 0L
}