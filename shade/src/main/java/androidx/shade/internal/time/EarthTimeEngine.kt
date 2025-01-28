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
import kotlin.math.absoluteValue

internal object EarthTimeEngine {
    private const val UPDATE_TIME = 12 * 60 * 60 * 1000L //12 hours
    internal const val TIME_SNAPSHOT = "androidx.shade.internal.time.EarthTimeEngine.snapshot"
    private var sync = false
    internal var deviceClock: DeviceClock = SystemDeviceClock

    fun now(): Long {
        val snapshot = getTimeSnapshot()
        val deviceWallClockMillis = deviceClock.currentTimeMillis()
        syncTime()
        return deviceWallClockMillis + snapshot.networkTimeOffsetMillis
    }

    private fun syncTime() {
        CoroutinesHolder.io.launch {
            val snapshot = getTimeSnapshot()
            val elapsedRealtimeSinceSyncMillis = deviceClock.elapsedRealtime() - snapshot.elapsedRealtimeAtSyncMillis
            val wallClockElapsedSinceSyncMillis = deviceClock.currentTimeMillis() - snapshot.deviceWallClockAtSyncMillis
            val clockDriftMillis = (elapsedRealtimeSinceSyncMillis - wallClockElapsedSinceSyncMillis).absoluteValue
            if (elapsedRealtimeSinceSyncMillis !in 0..UPDATE_TIME || clockDriftMillis > 1000) {
                if (sync) return@launch
                sync = true
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
                sync = false
            }
        }
    }

    private fun getTimeSnapshot(): TimeSnapshot {
        val json = CacheBox.getString(TIME_SNAPSHOT, "{}")
        return AppGson.toObject(json)
    }

    private fun getTimeByNTP(server: String) {
        runCatching {
            val socket = DatagramSocket()
            socket.soTimeout = 10 * 1000

            val address = InetAddress.getByName(server)
            val request = ByteArray(48)
            request[0] = 27.toByte()
            val packet = DatagramPacket(request, request.size, address, 123)
            socket.send(packet)

            val response = ByteArray(48)
            val responsePacket = DatagramPacket(response, response.size)
            socket.receive(responsePacket)
            socket.close()

            val seconds = ByteBuffer.wrap(response, 40, 4).order(ByteOrder.BIG_ENDIAN).getInt().toLong() and 0xffffffffL
            val fraction = ByteBuffer.wrap(response, 44, 4).order(ByteOrder.BIG_ENDIAN).getInt().toLong() and 0xffffffffL
            val timeInMillis = (seconds - 2208988800L) * 1000 + fraction * 1000L / 0x100000000L

            val deviceWallClockAtSyncMillis = deviceClock.currentTimeMillis()
            val snapshot = TimeSnapshot(
                deviceWallClockAtSyncMillis = deviceWallClockAtSyncMillis,
                elapsedRealtimeAtSyncMillis = deviceClock.elapsedRealtime(),
                networkTimeOffsetMillis = timeInMillis - deviceWallClockAtSyncMillis,
            )
            CacheBox.putString(TIME_SNAPSHOT, AppGson.toJson(snapshot))
            InternalLogUtil.verbose("EarthTime.getTimeByNTP success from $server")
        }.getOrElse {
            InternalLogUtil.verbose("EarthTime.getTimeByNTP failure from $server => ${it.message}")
        }
    }
}