package androidx.shade.internal.time

import androidx.shade.util.CacheBox
import androidx.shade.util.InternalLogUtil
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppGson
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.absoluteValue

internal object EarthTimeEngine {
    private const val UPDATE_TIME = 12 * 60 * 60 * 1000L //12 hours
    private const val NTP_PACKET_SIZE = 48
    private const val NTP_RECEIVE_TIMEOUT_MILLIS = 10 * 1000
    private const val NTP_TIMESTAMP_OFFSET_SECONDS = 2208988800L
    private const val NTP_TIMESTAMP_FRACTION_SCALE = 0x100000000L
    internal const val TIME_SNAPSHOT = "androidx.shade.internal.time.EarthTimeEngine.snapshot"
    private val sync = AtomicBoolean(false)
    private val nextSyncCheckElapsedRealtimeMillis = AtomicLong(0)
    internal var deviceClock: DeviceClock = SystemDeviceClock
    private val snapshot = AtomicReference(loadTimeSnapshot())

    fun now(): Long {
        val currentSnapshot = snapshot.get()
        val elapsedRealtimeMillis = deviceClock.elapsedRealtime()
        if (elapsedRealtimeMillis >= nextSyncCheckElapsedRealtimeMillis.get() && sync.compareAndSet(false, true)) syncTime()
        val elapsedRealtimeSinceSyncMillis = elapsedRealtimeMillis - currentSnapshot.elapsedRealtimeAtSyncMillis
        val mayHaveRebooted = elapsedRealtimeSinceSyncMillis < 0
        return when {
            // Unsynchronized
            currentSnapshot.deviceWallClockAtSyncMillis <= 0 -> deviceClock.currentTimeMillis() + currentSnapshot.networkTimeOffsetMillis
            // Possible device reboot
            mayHaveRebooted -> {
                // This heuristic cannot detect a reboot if the current uptime has already exceeded the persisted elapsed-realtime value.
                deviceClock.currentTimeMillis() + currentSnapshot.networkTimeOffsetMillis
            }
            // Normal
            else -> currentSnapshot.deviceWallClockAtSyncMillis + currentSnapshot.networkTimeOffsetMillis + elapsedRealtimeSinceSyncMillis
        }
    }

    private fun syncTime() {
        CoroutinesHolder.io.launch {
            val currentSnapshot = snapshot.get()
            val elapsedRealtimeSinceSyncMillis = deviceClock.elapsedRealtime() - currentSnapshot.elapsedRealtimeAtSyncMillis
            val wallClockElapsedSinceSyncMillis = deviceClock.currentTimeMillis() - currentSnapshot.deviceWallClockAtSyncMillis
            val clockDriftMillis = (elapsedRealtimeSinceSyncMillis - wallClockElapsedSinceSyncMillis).absoluteValue
            if (elapsedRealtimeSinceSyncMillis !in 0..UPDATE_TIME || clockDriftMillis > 1000) {
                InternalLogUtil.verbose("EarthTime.syncTime")
                coroutineScope {
                    launch {
                        getTimeByNTP("time.google.com")
                    }
                    launch {
                        getTimeByNTP("time.apple.com")
                    }
                    launch {
                        getTimeByNTP("time.windows.com")
                    }
                    launch {
                        getTimeByNTP("pool.ntp.org")
                    }
                }
                val updatedSnapshot = snapshot.get()
                if (updatedSnapshot !== currentSnapshot) {
                    nextSyncCheckElapsedRealtimeMillis.set(updatedSnapshot.elapsedRealtimeAtSyncMillis + UPDATE_TIME)
                }
            } else {
                nextSyncCheckElapsedRealtimeMillis.set(currentSnapshot.elapsedRealtimeAtSyncMillis + UPDATE_TIME)
            }
        }.invokeOnCompletion { sync.set(false) }
    }

    private fun loadTimeSnapshot(): TimeSnapshot {
        val json = CacheBox.getString(TIME_SNAPSHOT, "{}")
        return AppGson.toObject(json)
    }

    private fun getTimeByNTP(server: String) {
        runCatching {
            DatagramSocket().use { datagramSocket ->
                datagramSocket.soTimeout = NTP_RECEIVE_TIMEOUT_MILLIS

                val address = InetAddress.getByName(server)
                datagramSocket.connect(address, 123)
                val request = ByteArray(NTP_PACKET_SIZE)
                request[0] = 27.toByte()
                writeNtpTimestamp(request, deviceClock.currentTimeMillis())
                val requestTransmitTimestamp = request.copyOfRange(40, NTP_PACKET_SIZE)
                val packet = DatagramPacket(request, request.size)
                datagramSocket.send(packet)

                val response = ByteArray(NTP_PACKET_SIZE)
                val responsePacket = DatagramPacket(response, response.size)
                datagramSocket.receive(responsePacket)
                validateNtpResponse(responsePacket, requestTransmitTimestamp)

                val timeInMillis = readNtpTimestamp(response, 40)
                val deviceWallClockAtSyncMillis = deviceClock.currentTimeMillis()
                val newSnapshot = TimeSnapshot(
                    deviceWallClockAtSyncMillis = deviceWallClockAtSyncMillis,
                    elapsedRealtimeAtSyncMillis = deviceClock.elapsedRealtime(),
                    networkTimeOffsetMillis = timeInMillis - deviceWallClockAtSyncMillis,
                )
                snapshot.set(newSnapshot)
                CacheBox.putString(TIME_SNAPSHOT, AppGson.toJson(newSnapshot))
            }
        }.onSuccess {
            InternalLogUtil.verbose("EarthTime.getTimeByNTP success from $server")
        }.onFailure {
            InternalLogUtil.verbose("EarthTime.getTimeByNTP failure from $server => ${it.message}")
        }
    }

    private fun validateNtpResponse(responsePacket: DatagramPacket, requestTransmitTimestamp: ByteArray) {
        require(responsePacket.length >= NTP_PACKET_SIZE) { "invalid response length: ${responsePacket.length}" }

        val response = responsePacket.data
        val leapIndicator = response[0].toInt() ushr 6 and 0x03
        val version = response[0].toInt() ushr 3 and 0x07
        val mode = response[0].toInt() and 0x07
        val stratum = response[1].toInt() and 0xff
        require(leapIndicator != 3) { "server is unsynchronized" }
        require(version in 3..4) { "unsupported NTP version: $version" }
        require(mode == 4) { "invalid response mode: $mode" }
        require(stratum in 1..15) { "invalid stratum: $stratum" }
        require(response.copyOfRange(24, 32).contentEquals(requestTransmitTimestamp)) { "originate timestamp mismatch" }

        require(!isNtpTimestampZero(response, 32)) { "missing receive timestamp" }
        require(!isNtpTimestampZero(response, 40)) { "missing transmit timestamp" }
        val receiveTimeMillis = readNtpTimestamp(response, 32)
        val transmitTimeMillis = readNtpTimestamp(response, 40)
        require(transmitTimeMillis >= receiveTimeMillis) { "transmit timestamp precedes receive timestamp" }
    }

    private fun writeNtpTimestamp(target: ByteArray, timeInMillis: Long) {
        val seconds = timeInMillis / 1000 + NTP_TIMESTAMP_OFFSET_SECONDS
        val fraction = timeInMillis % 1000 * NTP_TIMESTAMP_FRACTION_SCALE / 1000
        ByteBuffer.wrap(target, 40, 8).order(ByteOrder.BIG_ENDIAN)
            .putInt(seconds.toInt())
            .putInt(fraction.toInt())
    }

    private fun readNtpTimestamp(source: ByteArray, offset: Int): Long {
        val buffer = ByteBuffer.wrap(source, offset, 8).order(ByteOrder.BIG_ENDIAN)
        val seconds = buffer.int.toLong() and 0xffffffffL
        val fraction = buffer.int.toLong() and 0xffffffffL
        return (seconds - NTP_TIMESTAMP_OFFSET_SECONDS) * 1000 + fraction * 1000L / NTP_TIMESTAMP_FRACTION_SCALE
    }

    private fun isNtpTimestampZero(source: ByteArray, offset: Int): Boolean = (offset until offset + 8).all { source[it] == 0.toByte() }
}