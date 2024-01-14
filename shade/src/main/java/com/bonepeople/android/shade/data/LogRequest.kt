package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

internal class LogRequest {
    @SerializedName("appName")
    var appName = ""

    @SerializedName("userId")
    var userId = ""//用户ID

    @SerializedName("androidId")
    var androidId = ""//系统ID

    @SerializedName("type")
    var type = ""//日志类型

    @SerializedName("code")
    var code = 0//事件代码

    @SerializedName("name")
    var name = ""//事件名称

    @SerializedName("message")
    var message = ""//日志信息

    @SerializedName("time")
    var time = 0L//日志时间戳
}