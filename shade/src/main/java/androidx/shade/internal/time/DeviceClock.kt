package androidx.shade.internal.time

internal interface DeviceClock {
    fun currentTimeMillis(): Long

    fun elapsedRealtime(): Long
}
