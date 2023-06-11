package com.bonepeople.android.shade

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
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
            if (config.state >= 5) return@launch
            val info = ConfigRequest().apply {
                androidId = AppSystem.androidId
                systemVersion = Build.VERSION.SDK_INT
                deviceModel = Build.MODEL
                deviceManufacturer = Build.MANUFACTURER
                packageName = ApplicationHolder.getPackageName()
                val signatures = ApplicationHolder.app.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
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
                    AppStorage.putString(CONFIG, it)
                    val config: Config = AppGson.toObject(it)
                    Protector.config = config
                }
                .onFailure { _, _ ->
                    config.offlineTimes--
                    if (config.offlineTimes < 0)
                        config.state = 4
                    AppStorage.putString(CONFIG, AppGson.toJson(config))
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
        check()
        return action.invoke()
    }

    private fun check() {
        CoroutinesHolder.default.launch {
            when (config.state) {
                0, 1 -> { //1-正常
                }
                2 -> { //2-警告
                    if (AppRandom.randomInt(1..100) < 30) {
                        delay(AppRandom.randomInt(20..60) * 1000L)
                        throw IllegalStateException()
                    }
                }
                3 -> { //3-威慑
                    if (AppRandom.randomInt(1..100) < 70) {
                        AppToast.show("当前APP未获得官方授权，可能存在未知问题")
                        delay(AppRandom.randomInt(20..60) * 1000L)
                        throw IllegalStateException()
                    }
                }
                4 -> { //4-禁用
                    delay(AppRandom.randomInt(10..40) * 1000L)
                    throw IllegalStateException()
                }
                else -> { //5-终止
                    AppToast.show("当前APP为非法程序，请停止使用", Toast.LENGTH_LONG)
                    delay(AppRandom.randomInt(10..20) * 1000L)
                    throw IllegalStateException()
                }
            }
        }
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