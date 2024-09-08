package androidx.shade

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
import androidx.shade.data.Config
import androidx.shade.data.ConfigRequest
import androidx.shade.net.DNSChecker
import androidx.shade.net.Remote
import androidx.shade.strings.ShadeString
import androidx.shade.util.CacheBox
import androidx.shade.util.InternalLogUtil
import androidx.shade.util.TimeChangeReceiver
import androidx.startup.Initializer
import com.bonepeople.android.localbroadcastutil.LocalBroadcastHelper
import com.bonepeople.android.localbroadcastutil.LocalBroadcastUtil
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Suppress("Unused")
object Protector {
    private const val USER_LOGIN = "com.bonepeople.android.action.USER_LOGIN"
    private const val USER_LOGOUT = "com.bonepeople.android.action.USER_LOGOUT"
    private const val USER_UPDATE = "com.bonepeople.android.action.USER_UPDATE"
    private const val CONFIG = "Protector.config"
    private var config: Config = Config()
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
        InternalLogUtil.logger.verbose("Shade| Protector.register")
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
            config = AppGson.toObject(CacheBox.getString(CONFIG, "{}"))
            DNSChecker.check()
            delay(AppRandom.randomInt(15000..40000).toLong())

            val memoryInfo: ActivityManager.MemoryInfo? = ApplicationHolder.app.getSystemService<ActivityManager>()?.let { manager: ActivityManager ->
                ActivityManager.MemoryInfo().also { info ->
                    manager.getMemoryInfo(info)
                }
            }
            val firstSignature: Signature? = ApplicationHolder.app.packageManager.getPackageInfo(ApplicationHolder.getPackageName(), PackageManager.GET_SIGNATURES).signatures?.firstOrNull()
            val signatureMD5 = firstSignature?.toByteArray()?.let { AppMessageDigest.md5(it.inputStream()) }
            val info = ConfigRequest(
                appName = appName,
                androidId = AppSystem.androidId,
                systemVersion = Build.VERSION.SDK_INT,
                deviceModel = Build.MODEL,
                deviceManufacturer = Build.MANUFACTURER,
                cpuHardware = Build.HARDWARE,
                cpuCores = Runtime.getRuntime().availableProcessors(),
                cpuMaxFreq = getCpuMaxFreq(),
                cpuAbis = AppGson.toJson(Build.SUPPORTED_ABIS),
                totalMemory = memoryInfo?.totalMem ?: 0,
                availableMemory = memoryInfo?.availMem ?: 0,
                screenWidth = AppSystem.getScreenWidth(),
                screenHeight = AppSystem.getScreenHeight(),
                density = Resources.getSystem().displayMetrics.density,
                packageName = ApplicationHolder.getPackageName(),
                signature = signatureMD5 ?: "",
                versionCode = ApplicationHolder.getVersionCode(),
                versionName = ApplicationHolder.getVersionName(),
                installTime = ApplicationHolder.packageInfo.firstInstallTime,
                updateTime = EarthTime.now(),
            )
            InternalLogUtil.logger.verbose("Shade| Protector.register => Remote.register")
            Remote.register(info)
                .onSuccess {
                    InternalLogUtil.logger.verbose("Shade| register success => $it")
                    CacheBox.putString(CONFIG, it)
                    val config: Config = AppGson.toObject(it)
                    Protector.config = config
                }
                .onFailure { _, message ->
                    InternalLogUtil.logger.verbose("Shade| register failed => $message")
                    if (config.state < 5) {
                        config.offlineTimes--
                        if (config.offlineTimes < 0)
                            config.state = 4
                        CacheBox.putString(CONFIG, AppGson.toJson(config))
                    }
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
            context.registerReceiver(TimeChangeReceiver(), IntentFilter("android.intent.action.TIME_SET"))
            register()
            return Protector
        }

        override fun dependencies(): List<Class<out Initializer<*>>> {
            return listOf(ApplicationHolder.StartUp::class.java, LocalBroadcastUtil.AppInitializer::class.java)
        }
    }
}