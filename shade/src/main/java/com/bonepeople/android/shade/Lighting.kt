package com.bonepeople.android.shade

import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.shade.net.Remote
import com.bonepeople.android.widget.util.AppSystem

object Lighting {
    suspend fun c5(type: String, code: Int, name: String, message: String) {
        if (Protector.skipLog(type)) return
        val info = LogRequest().apply {
            appName = Protector.appName
            androidId = AppSystem.androidId
            this.type = type
            this.code = code
            this.name = name
            this.message = message
            time = EarthTime.now()
        }
        Remote.log(info)
    }
}