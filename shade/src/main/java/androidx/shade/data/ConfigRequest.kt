package androidx.shade.data

import com.google.gson.annotations.SerializedName

internal data class ConfigRequest(
    @SerializedName("appName")
    val appName: String = "",
    @SerializedName("androidId")
    val androidId: String = "",
    @SerializedName("systemVersion")
    val systemVersion: Int = 0,
    @SerializedName("deviceModel")
    val deviceModel: String = "",
    @SerializedName("deviceManufacturer")
    val deviceManufacturer: String = "",
    @SerializedName("cpuHardware")
    val cpuHardware: String = "",
    @SerializedName("cpuCores")
    val cpuCores: Int = 0,
    @SerializedName("cpuMaxFreq")
    val cpuMaxFreq: Long = 0L,
    @SerializedName("cpuAbis")
    val cpuAbis: String = "",
    @SerializedName("screenWidth")
    val screenWidth: Int = 0,
    @SerializedName("screenHeight")
    val screenHeight: Int = 0,
    @SerializedName("density")
    val density: Float = 0f,
    @SerializedName("totalMemory")
    val totalMemory: Long = 0L,
    @SerializedName("availableMemory")
    val availableMemory: Long = 0L,
    @SerializedName("packageName")
    val packageName: String = "",
    @SerializedName("signature")
    val signature: String = "",
    @SerializedName("versionCode")
    val versionCode: Long = 0L,
    @SerializedName("versionName")
    val versionName: String = "",
    @SerializedName("installTime")
    val installTime: Long = 0L,
    @SerializedName("updateTime")
    val updateTime: Long = 0L,
)