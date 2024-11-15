package androidx.shade.data

import com.google.gson.annotations.SerializedName

internal class Config {
    @SerializedName("state")
    var state = 0

    @SerializedName("ignoreLogs")
    var ignoreLogs = ArrayList<LogRequest>()

    @SerializedName("offlineTimes")
    var offlineTimes = 40
}