package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

class ConfigRequest {
    @SerializedName("userId")
    var userId = ""//用户ID

    @SerializedName("androidId")
    var androidId = ""//系统ID

    @SerializedName("systemVersion")
    var systemVersion = 0//系统版本

    @SerializedName("deviceModel")
    var deviceModel = ""//手机型号

    @SerializedName("deviceManufacturer")
    var deviceManufacturer = ""//手机品牌

    @SerializedName("versionCode")
    var versionCode = 0//app版本号

    @SerializedName("versionName")
    var versionName = ""//app版本名称

    @SerializedName("installTime")
    var installTime = 0L//安装日期

    @SerializedName("updateTime")
    var updateTime = 0L//上报时间
}