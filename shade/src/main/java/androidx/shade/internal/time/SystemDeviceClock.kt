package androidx.shade.internal.time

import android.os.SystemClock

internal object SystemDeviceClock : DeviceClock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
}
