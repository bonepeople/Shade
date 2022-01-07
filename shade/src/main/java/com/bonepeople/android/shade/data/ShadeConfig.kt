package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

internal class ShadeConfig {
    @SerializedName("path")
    var path = ""

    @SerializedName("secret")
    var secret = ""

    @SerializedName("salt")
    var salt = ""
}