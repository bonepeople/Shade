package com.bonepeople.android.shade

import android.os.SystemClock
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

object EarthTime {
    private const val UPDATE_TIME = 12 * 60 * 60 * 1000L //12小时
    private const val TIME_OFFSET = "com.bonepeople.android.shade.EarthTime.offset" //本地时间与标准时间的偏移量
    private const val TIME_LAST = "com.bonepeople.android.shade.EarthTime.lastTime" //上一次更新时间的时间戳
    private var sync = false

    fun now(): Long {
        val offset = AppStorage.getLong(TIME_OFFSET, 0)
        val systemTime = System.currentTimeMillis()
        syncTime()
        return systemTime + offset
    }

    private fun syncTime() {
        CoroutinesHolder.default.launch(Dispatchers.IO) {
            val current = SystemClock.elapsedRealtime()
            val lastTime = AppStorage.getLong(TIME_LAST, 0)
            val elapsed = current - lastTime
            if (elapsed < 0 || elapsed > UPDATE_TIME) {
                if (sync) return@launch
                sync = true
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

            val offset = timeInMillis - System.currentTimeMillis()
            AppStorage.putLong(TIME_LAST, SystemClock.elapsedRealtime())
            AppStorage.putLong(TIME_OFFSET, offset)
        }
    }
}