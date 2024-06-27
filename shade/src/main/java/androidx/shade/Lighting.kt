package androidx.shade

import androidx.shade.data.LogRequest
import androidx.shade.net.Remote
import com.bonepeople.android.widget.util.AppSystem

object Lighting {
    suspend fun c5(type: String, code: Int, name: String, message: String) {
        if (Protector.skipLog(type)) return
        val info = LogRequest(
            appName = Protector.appName,
            androidId = AppSystem.androidId,
            type = type,
            code = code,
            name = name,
            message = message,
            time = EarthTime.now(),
        )
        Remote.log(info)
    }
}