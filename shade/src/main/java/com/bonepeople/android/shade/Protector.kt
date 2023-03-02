package com.bonepeople.android.shade

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.startup.Initializer
import com.bonepeople.android.localbroadcastutil.LocalBroadcastHelper
import com.bonepeople.android.localbroadcastutil.LocalBroadcastUtil
import com.bonepeople.android.shade.data.Config
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.shade.net.Remote
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Protector {
    private const val USER_LOGIN = "com.bonepeople.android.action.USER_LOGIN"
    private const val USER_LOGOUT = "com.bonepeople.android.action.USER_LOGOUT"
    private const val USER_UPDATE = "com.bonepeople.android.action.USER_UPDATE"
    private const val CONFIG = "Protector.config"
    private const val ROOT = "N6H95wiH4UoP4N6c"
    private var config: Config = AppGson.toObject(AppStorage.getString(CONFIG, "{}"))

    @SuppressLint("PackageManagerGetSignatures")
    private fun register() {
        LocalBroadcastHelper.register(null, USER_LOGIN, USER_LOGOUT, USER_UPDATE) {
            CoroutinesHolder.default.launch {
                when (it.action) {
                    USER_LOGIN -> c5("shade.user", 1, "login", "")
                    USER_LOGOUT -> c5("shade.user", 2, "logout", "")
                    USER_UPDATE -> c5("shade.user", 3, "update", "")
                }
            }
        }
        CoroutinesHolder.default.launch {
            val time = AppRandom.randomInt(2..30).toLong()
            delay(time * 1000)
            if (AppStorage.getBoolean(ROOT)) return@launch
            val info = ConfigRequest().apply {
                androidId = AppSystem.androidId
                systemVersion = Build.VERSION.SDK_INT
                deviceModel = Build.MODEL
                deviceManufacturer = Build.MANUFACTURER
                packageName = ApplicationHolder.getPackageName()
                val signatures = ApplicationHolder.instance.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
                if (!signatures.isNullOrEmpty()) {
                    signature = AppEncrypt.encryptByMD5(signatures[0].toByteArray().inputStream())
                }
                versionCode = ApplicationHolder.getVersionCode()
                versionName = ApplicationHolder.getVersionName()
                installTime = ApplicationHolder.packageInfo.firstInstallTime
                updateTime = System.currentTimeMillis()
            }
            Remote.register(info)
                .onSuccess {
                    val config: Config = AppGson.toObject(it)
                    AppStorage.putString(CONFIG, AppGson.toJson(config))
                    Protector.config = config
                    when (config.state) {
                        2 -> {
                            AppStorage.putBoolean(ROOT, true)
                        }
                    }
                }
        }
    }

    suspend fun c5(type: String, code: Int, name: String, message: String) {
        config.ignoreLogs.forEach {
            if (type == it.type)
                return
        }
        val info = LogRequest().apply {
            userId = AppStorage.getString("com.bonepeople.android.key.USER_ID")
            androidId = AppSystem.androidId
            this.type = type
            this.code = code
            this.name = name
            this.message = message
            time = System.currentTimeMillis()
        }
        Remote.log(info)
    }

    fun <T> protect(action: () -> T): T {
        if (AppStorage.getBoolean(ROOT, false) && AppRandom.randomInt(1..100) < 50) {
            CoroutinesHolder.default.launch {
                delay(AppRandom.randomInt(10..60) * 1000L)
                throw IllegalStateException()
            }
        }
        return action.invoke()
    }

    class StartUp : Initializer<Protector> {
        override fun create(context: Context): Protector {
            register()
            return Protector
        }

        override fun dependencies(): List<Class<out Initializer<*>>> {
            return listOf(ApplicationHolder.StartUp::class.java, LocalBroadcastUtil.AppInitializer::class.java)
        }
    }
}