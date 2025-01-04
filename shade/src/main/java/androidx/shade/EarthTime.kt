package androidx.shade

import androidx.shade.internal.time.EarthTimeEngine

object EarthTime {
    fun now(): Long = EarthTimeEngine.now()
}
