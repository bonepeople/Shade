package androidx.shade.data

import com.google.gson.annotations.SerializedName
import java.util.UUID

internal data class LogRequest(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("appName")
    val appName: String = "",
    @SerializedName("userId")
    val userId: String = "",
    @SerializedName("androidId")
    val androidId: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("time")
    val time: Long = 0L,
)