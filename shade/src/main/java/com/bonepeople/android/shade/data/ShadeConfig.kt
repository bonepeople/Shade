package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

internal class ShadeConfig {
    @SerializedName("path")
    var path = "http://192.168.1.1:8192/app/karo"

    @SerializedName("secret")
    var secret = ""

    @SerializedName("salt")
    var salt = ""
}