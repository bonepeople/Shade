package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

internal class Environment {
    @SerializedName("authorized")
    var authorized = true

    @SerializedName("needUpload")
    var needUpload = false
}