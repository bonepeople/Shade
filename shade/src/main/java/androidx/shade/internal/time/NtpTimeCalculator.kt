package androidx.shade.internal.time

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object NtpTimeCalculator {
    private const val NTP_TIMESTAMP_OFFSET_SECONDS = 2208988800L
    private const val NTP_TIMESTAMP_FRACTION_SCALE = 0x100000000L
    private const val NTP_HALF_ERA_SECONDS = 0x80000000L

    fun writeTransmitTimestamp(target: ByteArray, timeInMillis: Long) {
        val seconds = addExact(floorDiv(timeInMillis, 1000), NTP_TIMESTAMP_OFFSET_SECONDS)
        val fraction = floorMod(timeInMillis, 1000) * NTP_TIMESTAMP_FRACTION_SCALE / 1000
        ByteBuffer.wrap(target, 40, 8).order(ByteOrder.BIG_ENDIAN)
            .putInt(seconds.toInt())
            .putInt(fraction.toInt())
    }

    fun readTimestamp(source: ByteArray, offset: Int, referenceTimeMillis: Long): Long {
        val buffer = ByteBuffer.wrap(source, offset, 8).order(ByteOrder.BIG_ENDIAN)
        val rawSeconds = buffer.int.toLong() and 0xffffffffL
        val fraction = buffer.int.toLong() and 0xffffffffL
        val referenceSeconds = addExact(floorDiv(referenceTimeMillis, 1000), NTP_TIMESTAMP_OFFSET_SECONDS)
        val era = floorDiv(
            addExact(subtractExact(referenceSeconds, rawSeconds), NTP_HALF_ERA_SECONDS),
            NTP_TIMESTAMP_FRACTION_SCALE,
        )
        val eraSeconds = multiplyExact(era, NTP_TIMESTAMP_FRACTION_SCALE)
        val seconds = addExact(rawSeconds, eraSeconds)
        val unixSeconds = subtractExact(seconds, NTP_TIMESTAMP_OFFSET_SECONDS)
        val millis = multiplyExact(unixSeconds, 1000)
        return addExact(millis, fraction * 1000L / NTP_TIMESTAMP_FRACTION_SCALE)
    }

    fun calculateOffsetMillis(t1: Long, t2: Long, t3: Long, t4: Long): Long {
        val requestOffsetMillis = subtractExact(t2, t1)
        val responseOffsetMillis = subtractExact(t3, t4)
        return addExact(requestOffsetMillis, responseOffsetMillis) / 2
    }

    fun calculateRoundTripMillis(clientElapsedMillis: Long, serverProcessingMillis: Long): Long =
        subtractExact(clientElapsedMillis, serverProcessingMillis)

    fun addExact(left: Long, right: Long): Long {
        val result = left + right
        require(((left xor result) and (right xor result)) >= 0) { "long addition overflow" }
        return result
    }

    fun subtractExact(left: Long, right: Long): Long {
        val result = left - right
        require(((left xor right) and (left xor result)) >= 0) { "long subtraction overflow" }
        return result
    }

    private fun multiplyExact(left: Long, right: Long): Long {
        require(left != -1L || right != Long.MIN_VALUE) { "long multiplication overflow" }
        require(right != -1L || left != Long.MIN_VALUE) { "long multiplication overflow" }
        val result = left * right
        require(left == 0L || result / left == right) { "long multiplication overflow" }
        return result
    }

    private fun floorDiv(value: Long, divisor: Long): Long {
        require(divisor > 0) { "divisor must be positive" }
        val quotient = value / divisor
        return if (value % divisor < 0) quotient - 1 else quotient
    }

    private fun floorMod(value: Long, divisor: Long): Long {
        require(divisor > 0) { "divisor must be positive" }
        val remainder = value % divisor
        return if (remainder < 0) remainder + divisor else remainder
    }
}