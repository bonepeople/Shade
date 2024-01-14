package com.bonepeople.android.shade.data

import com.google.gson.annotations.SerializedName

internal class ConfigRequest {
    @SerializedName("appName")
    var appName = ""

    @SerializedName("androidId")
    var androidId = ""//系统ID

    @SerializedName("systemVersion")
    var systemVersion = 0//系统版本

    @SerializedName("deviceModel")
    var deviceModel = ""//手机型号

    @SerializedName("deviceManufacturer")
    var deviceManufacturer = ""//手机品牌

    @SerializedName("cpuHardware")
    var cpuHardware = ""

    @SerializedName("cpuCores")
    var cpuCores = 0

    @SerializedName("cpuMaxFreq")
    var cpuMaxFreq = 0L

    @SerializedName("cpuAbis")
    var cpuAbis = ""

    @SerializedName("totalMemory")
    var totalMemory = 0L

    @SerializedName("availableMemory")
    var availableMemory = 0L

    @SerializedName("packageName")
    var packageName = ""

    @SerializedName("signature")
    var signature = ""//APP签名的MD5信息

    @SerializedName("versionCode")
    var versionCode = 0L//app版本号

    @SerializedName("versionName")
    var versionName = ""//app版本名称

    @SerializedName("installTime")
    var installTime = 0L//安装日期

    @SerializedName("updateTime")
    var updateTime = 0L//上报时间
}