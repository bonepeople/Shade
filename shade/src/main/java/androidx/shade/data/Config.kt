package androidx.shade.data

import com.google.gson.annotations.SerializedName

internal data class Config(
    @SerializedName("state")
    var state: Int = 0,
    @SerializedName("ignoreLogs")
    val ignoreLogs: List<LogRequest> = listOf(),
    @SerializedName("offlineTimes")
    var offlineTimes: Int = 40,
)