package androidx.shade.internal.time

import com.google.gson.annotations.SerializedName

internal data class TimeSnapshot(
    // Device wall-clock reading captured at synchronization.
    @SerializedName("deviceWallClockAtSyncMillis")
    val deviceWallClockAtSyncMillis: Long = 0,
    // Monotonic clock reading captured at synchronization.
    @SerializedName("elapsedRealtimeAtSyncMillis")
    val elapsedRealtimeAtSyncMillis: Long = 0,
    // Offset added to the device wall clock to estimate network time.
    @SerializedName("networkTimeOffsetMillis")
    val networkTimeOffsetMillis: Long = 0,
)