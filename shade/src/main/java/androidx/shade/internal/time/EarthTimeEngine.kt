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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.absoluteValue

internal object EarthTimeEngine {
    private const val UPDATE_TIME = 12 * 60 * 60 * 1000L //12 hours
    private const val NTP_PACKET_SIZE = 48
    private const val NTP_RECEIVE_TIMEOUT_MILLIS = 10 * 1000
    private val NTP_SERVERS = listOf("time.google.com", "time.apple.com", "time.windows.com", "pool.ntp.org")
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
                    NTP_SERVERS.forEach { server ->
                        launch {
                            runCatching {
                                val newSnapshot = getTimeByNTP(server)
                                snapshot.set(newSnapshot)
                                CacheBox.putString(TIME_SNAPSHOT, AppGson.toJson(newSnapshot))
                            }.onSuccess {
                                InternalLogUtil.verbose("EarthTime.getTimeByNTP success from $server")
                            }.onFailure {
                                InternalLogUtil.verbose("EarthTime.getTimeByNTP failure from $server => ${it.message}")
                            }
                        }
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

    private fun getTimeByNTP(server: String): TimeSnapshot {
        return DatagramSocket().use { datagramSocket ->
            datagramSocket.soTimeout = NTP_RECEIVE_TIMEOUT_MILLIS

            val address = InetAddress.getByName(server)
            datagramSocket.connect(address, 123)
            val request = ByteArray(NTP_PACKET_SIZE)
            request[0] = 27.toByte()
            val requestTimeMillis = deviceClock.currentTimeMillis()
            NtpTimeCalculator.writeTransmitTimestamp(request, requestTimeMillis)
            val requestTransmitTimestamp = request.copyOfRange(40, NTP_PACKET_SIZE)
            val packet = DatagramPacket(request, request.size)
            val requestElapsedRealtimeMillis = deviceClock.elapsedRealtime()
            datagramSocket.send(packet)

            val response = ByteArray(NTP_PACKET_SIZE)
            val responsePacket = DatagramPacket(response, response.size)
            datagramSocket.receive(responsePacket)
            val responseElapsedRealtimeMillis = deviceClock.elapsedRealtime()
            validateNtpResponse(responsePacket, requestTransmitTimestamp)

            val clientElapsedMillis = NtpTimeCalculator.subtractExact(responseElapsedRealtimeMillis, requestElapsedRealtimeMillis)
            require(clientElapsedMillis >= 0) { "elapsed realtime moved backwards" }
            val responseTimeMillis = NtpTimeCalculator.addExact(requestTimeMillis, clientElapsedMillis)
            val receiveTimeMillis = NtpTimeCalculator.readTimestamp(response, 32, responseTimeMillis)
            val transmitTimeMillis = NtpTimeCalculator.readTimestamp(response, 40, responseTimeMillis)
            val serverProcessingMillis = NtpTimeCalculator.subtractExact(transmitTimeMillis, receiveTimeMillis)
            require(serverProcessingMillis >= 0) { "transmit timestamp precedes receive timestamp" }
            val roundTripMillis = NtpTimeCalculator.calculateRoundTripMillis(clientElapsedMillis, serverProcessingMillis)
            require(roundTripMillis >= 0) { "invalid round trip: $roundTripMillis ms" }
            val offsetMillis = NtpTimeCalculator.calculateOffsetMillis(requestTimeMillis, receiveTimeMillis, transmitTimeMillis, responseTimeMillis)
            val networkTimeAtResponseMillis = NtpTimeCalculator.addExact(responseTimeMillis, offsetMillis)

            val deviceWallClockAtSyncMillis = deviceClock.currentTimeMillis()
            val elapsedRealtimeAtSyncMillis = deviceClock.elapsedRealtime()
            val elapsedRealtimeSinceResponseMillis = NtpTimeCalculator.subtractExact(elapsedRealtimeAtSyncMillis, responseElapsedRealtimeMillis)
            require(elapsedRealtimeSinceResponseMillis >= 0) { "elapsed realtime moved backwards" }
            val networkTimeAtSyncMillis = NtpTimeCalculator.addExact(networkTimeAtResponseMillis, elapsedRealtimeSinceResponseMillis)
            TimeSnapshot(
                deviceWallClockAtSyncMillis = deviceWallClockAtSyncMillis,
                elapsedRealtimeAtSyncMillis = elapsedRealtimeAtSyncMillis,
                networkTimeOffsetMillis = NtpTimeCalculator.subtractExact(networkTimeAtSyncMillis, deviceWallClockAtSyncMillis),
            )
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
    }

    private fun isNtpTimestampZero(source: ByteArray, offset: Int): Boolean = (offset until offset + 8).all { source[it] == 0.toByte() }
}