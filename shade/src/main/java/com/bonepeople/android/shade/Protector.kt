package com.bonepeople.android.shade

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Resources
import android.os.Build
import android.widget.Toast
import androidx.core.content.getSystemService
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
import java.io.File
import java.util.Locale

@Suppress("UNUSED")
object Protector {
    private const val USER_LOGIN = "com.bonepeople.android.action.USER_LOGIN"
    private const val USER_LOGOUT = "com.bonepeople.android.action.USER_LOGOUT"
    private const val USER_UPDATE = "com.bonepeople.android.action.USER_UPDATE"
    private const val CONFIG = "Protector.config"
    private var config: Config = AppGson.toObject(CacheBox.getString(CONFIG, "{}"))
    internal val appName by lazy {
        ApplicationHolder.packageInfo.applicationInfo.loadLabel(ApplicationHolder.app.packageManager).toString()
    }

    fun <T> protect(action: () -> T): T {
        check()
        return action.invoke()
    }

    internal fun skipLog(type: String): Boolean {
        config.ignoreLogs.forEach {
            if (type == it.type)
                return true
        }
        return false
    }

    private fun check() {
        CoroutinesHolder.default.launch {
            val name: String = ApplicationHolder.getPackageName()
            when (name) {
                "com.xizhi_ai.xizhi_higgz" -> return@launch
                "com.finshell.fin" -> return@launch
                "com.finshell.fin1" -> return@launch
            }
            when (config.state) {
                0, 1 -> {
                }

                2 -> {
                    if (AppRandom.randomInt(1..100) < 30) {
                        delay(AppRandom.randomInt(20..60) * 1000L)
                        Lighting.c5("shade.shutdown", 1, "IllegalState", "state = ${config.state}")
                        throw IllegalStateException("[$name] System Error 0x02")
                    }
                }

                3 -> {
                    if (AppRandom.randomInt(1..100) < 70) {
                        AppToast.show(StringResourceManager.get(ShadeString.templateClass).unAuthorized)
                        delay(AppRandom.randomInt(20..60) * 1000L)
                        Lighting.c5("shade.shutdown", 1, "IllegalState", "state = ${config.state}")
                        throw IllegalStateException("[$name] System Error 0x03")
                    }
                }

                4 -> {
                    delay(AppRandom.randomInt(10..40) * 1000L)
                    Lighting.c5("shade.shutdown", 1, "IllegalState", "state = ${config.state}")
                    throw IllegalStateException("[$name] System Error 0x04")
                }

                else -> {
                    AppToast.show(StringResourceManager.get(ShadeString.templateClass).illegal, Toast.LENGTH_LONG)
                    delay(AppRandom.randomInt(10..20) * 1000L)
                    Lighting.c5("shade.shutdown", 1, "IllegalState", "state = ${config.state}")
                    throw IllegalStateException("[$name] System Error 0x05")
                }
            }
        }
    }

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
            delay(AppRandom.randomInt(15000..40000).toLong())
            if (config.state >= 5) return@launch
            val info = ConfigRequest().apply {
                appName = Protector.appName
                androidId = AppSystem.androidId
                systemVersion = Build.VERSION.SDK_INT
                deviceModel = Build.MODEL
                deviceManufacturer = Build.MANUFACTURER
                cpuHardware = Build.HARDWARE
                cpuCores = Runtime.getRuntime().availableProcessors()
                cpuMaxFreq = getCpuMaxFreq()
                cpuAbis = AppGson.toJson(Build.SUPPORTED_ABIS)
                ApplicationHolder.app.getSystemService<ActivityManager>()?.let { manager ->
                    ActivityManager.MemoryInfo().let { memoryInfo ->
                        manager.getMemoryInfo(memoryInfo)
                        totalMemory = memoryInfo.totalMem
                        availableMemory = memoryInfo.availMem
                    }
                }
                screenWidth = AppSystem.getScreenWidth()
                screenHeight = AppSystem.getScreenHeight()
                density = Resources.getSystem().displayMetrics.density
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

    private fun getCpuMaxFreq(): Long {
        return kotlin.runCatching {
            File("sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq").readLines().firstOrNull()?.toLong() ?: 0
        }.getOrDefault(0)
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