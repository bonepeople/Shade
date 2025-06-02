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
    private const val NTP_RECEIVE_TIMEOUT_MILLIS = 10 * 1000
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
                val request = ByteArray(48)
                request[0] = 27.toByte()
                val packet = DatagramPacket(request, request.size)
                datagramSocket.send(packet)

                val response = ByteArray(48)
                val responsePacket = DatagramPacket(response, response.size)
                datagramSocket.receive(responsePacket)

                val seconds = ByteBuffer.wrap(response, 40, 4).order(ByteOrder.BIG_ENDIAN).getInt().toLong() and 0xffffffffL
                val fraction = ByteBuffer.wrap(response, 44, 4).order(ByteOrder.BIG_ENDIAN).getInt().toLong() and 0xffffffffL
                val timeInMillis = (seconds - 2208988800L) * 1000 + fraction * 1000L / 0x100000000L

                val deviceWallClockAtSyncMillis = deviceClock.currentTimeMillis()
                val newSnapshot = TimeSnapshot(
                    deviceWallClockAtSyncMillis = deviceWallClockAtSyncMillis,
                    elapsedRealtimeAtSyncMillis = deviceClock.elapsedRealtime(),
                    networkTimeOffsetMillis = timeInMillis - deviceWallClockAtSyncMillis,
                )
                snapshot.set(newSnapshot)
                CacheBox.putString(TIME_SNAPSHOT, AppGson.toJson(newSnapshot))
                InternalLogUtil.verbose("EarthTime.getTimeByNTP success from $server")
            }
        }.getOrElse {
            InternalLogUtil.verbose("EarthTime.getTimeByNTP failure from $server => ${it.message}")
        }
    }
}