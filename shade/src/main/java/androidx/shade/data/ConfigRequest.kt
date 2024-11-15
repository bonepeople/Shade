package androidx.shade.data

import com.google.gson.annotations.SerializedName

internal class ConfigRequest {
    @SerializedName("appName")
    var appName = ""

    @SerializedName("androidId")
    var androidId = ""

    @SerializedName("systemVersion")
    var systemVersion = 0

    @SerializedName("deviceModel")
    var deviceModel = ""

    @SerializedName("deviceManufacturer")
    var deviceManufacturer = ""

    @SerializedName("cpuHardware")
    var cpuHardware = ""

    @SerializedName("cpuCores")
    var cpuCores = 0

    @SerializedName("cpuMaxFreq")
    var cpuMaxFreq = 0L

    @SerializedName("cpuAbis")
    var cpuAbis = ""

    @SerializedName("screenWidth")
    var screenWidth = 0

    @SerializedName("screenHeight")
    var screenHeight = 0

    @SerializedName("density")
    var density = 0f

    @SerializedName("totalMemory")
    var totalMemory = 0L

    @SerializedName("availableMemory")
    var availableMemory = 0L

    @SerializedName("packageName")
    var packageName = ""

    @SerializedName("signature")
    var signature = ""

    @SerializedName("versionCode")
    var versionCode = 0L

    @SerializedName("versionName")
    var versionName = ""

    @SerializedName("installTime")
    var installTime = 0L

    @SerializedName("updateTime")
    var updateTime = 0L
}