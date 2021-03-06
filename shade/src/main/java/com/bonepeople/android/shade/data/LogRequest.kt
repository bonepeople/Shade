package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

internal class LogRequest {
    @SerializedName("userId")
    var userId = ""//用户ID

    @SerializedName("androidId")
    var androidId = ""//系统ID

    @SerializedName("flow")
    var flow = 0//流程ID

    @SerializedName("code")
    var code = 0//事件代码

    @SerializedName("name")
    var name = ""//事件名称

    @SerializedName("message")
    var message = ""//日志信息

    @SerializedName("time")
    var time = 0L//日志时间戳
}