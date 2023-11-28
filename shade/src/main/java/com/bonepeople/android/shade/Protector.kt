package com.bonepeople.android.shade

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.widget.Toast
import androidx.startup.Initializer
import com.bonepeople.android.localbroadcastutil.LocalBroadcastHelper
import com.bonepeople.android.localbroadcastutil.LocalBroadcastUtil
import com.bonepeople.android.shade.data.Config
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.net.Remote
import com.bonepeople.android.shade.strings.ShadeString
import com.bonepeople.android.shade.strings.ShadeStringEnUS
import com.bonepeople.android.shade.strings.ShadeStringZhCN
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("UNUSED")
object Protector {
    private const val USER_LOGIN = "com.bonepeople.android.action.USER_LOGIN"
    private const val USER_LOGOUT = "com.bonepeople.android.action.USER_LOGOUT"
    private const val USER_UPDATE = "com.bonepeople.android.action.USER_UPDATE"
    private const val CONFIG = "Protector.config"
    private var config: Config = AppGson.toObject(CacheBox.getString(CONFIG, "{}"))

    @SuppressLint("PackageManagerGetSignatures")
    private fun register() {
        LocalBroadcastHelper.register(null, USER_LOGIN, USER_LOGOUT, USER_UPDATE) {
            CoroutinesHolder.io.launch {
                when (it.action) {
                    USER_LOGIN -> Lighting.c5("shade.user", 1, "login", "")
                    USER_LOGOUT -> Lighting.c5("shade.user", 2, "logout", "")
                    USER_UPDATE -> Lighting.c5("shade.user", 3, "update", "")
                }
            }
        }
        CoroutinesHolder.default.launch {
            EarthTime.now()
            val time: Long = AppRandom.randomInt(10..40).toLong()
            delay(time * 1000)
            if (config.state >= 5) return@launch
            val info = ConfigRequest().apply {
                androidId = AppSystem.androidId
                systemVersion = Build.VERSION.SDK_INT
                deviceModel = Build.MODEL
                deviceManufacturer = Build.MANUFACTURER
                packageName = ApplicationHolder.getPackageName()
                val signatures: Array<Signature>? = ApplicationHolder.app.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
                if (!signatures.isNullOrEmpty()) {
                    signature = AppMessageDigest.md5(signatures[0].toByteArray().inputStream())
                }
                versionCode = ApplicationHolder.getVersionCode()
                versionName = ApplicationHolder.getVersionName()
                installTime = ApplicationHolder.packageInfo.firstInstallTime
                updateTime = EarthTime.now()
            }
            Remote.register(info)
                .onSuccess {
                    CacheBox.putString(CONFIG, it)
                    val config: Config = AppGson.toObject(it)
                    Protector.config = config
                }
                .onFailure { _, _ ->
                    config.offlineTimes--
                    if (config.offlineTimes < 0)
                        config.state = 4
                    CacheBox.putString(CONFIG, AppGson.toJson(config))
                }
        }
    }

    fun skipLog(type: String): Boolean {
        config.ignoreLogs.forEach {
            if (type == it.type)
                return true
        }
        return false
    }

    fun <T> protect(action: () -> T): T {
        check()
        return action.invoke()
    }

    private fun check() {
        CoroutinesHolder.default.launch {
            val name: String = ApplicationHolder.getPackageName()
            when (name) {
                "com.xizhi_ai.xizhi_higgz" -> return@launch
            }
            when (config.state) {
                0, 1 -> { //1-正常
                }

                2 -> { //2-警告
                    if (AppRandom.randomInt(1..100) < 30) {
                        delay(AppRandom.randomInt(20..60) * 1000L)
                        Lighting.c5("shade.shutdown", 1, "313", "")
                        throw IllegalStateException("[$name] System Error 0x02")
                    }
                }

                3 -> { //3-威慑
                    if (AppRandom.randomInt(1..100) < 70) {
                        AppToast.show(StringResourceManager.get(ShadeString.templateClass).unAuthorized)
                        delay(AppRandom.randomInt(20..60) * 1000L)
                        Lighting.c5("shade.shutdown", 1, "313", "")
                        throw IllegalStateException("[$name] System Error 0x03")
                    }
                }

                4 -> { //4-禁用
                    delay(AppRandom.randomInt(10..40) * 1000L)
                    Lighting.c5("shade.shutdown", 1, "313", "")
                    throw IllegalStateException("[$name] System Error 0x04")
                }

                else -> { //5-终止
                    AppToast.show(StringResourceManager.get(ShadeString.templateClass).illegal, Toast.LENGTH_LONG)
                    delay(AppRandom.randomInt(10..20) * 1000L)
                    Lighting.c5("shade.shutdown", 1, "313", "")
                    throw IllegalStateException("[$name] System Error 0x05")
                }
            }
        }
    }

    class StartUp : Initializer<Protector> {
        override fun create(context: Context): Protector {
            StringResourceManager.register(ShadeStringEnUS(), Locale.US)
            StringResourceManager.register(ShadeStringZhCN(), Locale.SIMPLIFIED_CHINESE)
            context.registerReceiver(TimeChangeReceiver(), IntentFilter("android.intent.action.TIME_SET"))
            register()
            return Protector
        }

        override fun dependencies(): List<Class<out Initializer<*>>> {
            return listOf(ApplicationHolder.StartUp::class.java, LocalBroadcastUtil.AppInitializer::class.java)
        }
    }
}