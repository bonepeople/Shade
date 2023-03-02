package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

internal class Config {
    @SerializedName("state")
    var state = 0

    @SerializedName("ignoreLogs")
    var ignoreLogs = ArrayList<LogRequest>()
}