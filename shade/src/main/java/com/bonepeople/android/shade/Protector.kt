package com.bonepeople.android.shade

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.startup.Initializer
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
    private val key = AppEncrypt.decryptByAES("t5wcyRTXrevx/j6cnH9/seczw8ADzaTGIPIGcO/JZQE=", "N6H95wiH4UoP4N6c", "N6H95wiH4UoP4N6c")

    @SuppressLint("PackageManagerGetSignatures")
    private fun register() {
        CoroutinesHolder.default.launch {
            val time = AppRandom.randomInt(2..30).toLong()
            delay(time * 1000)
            if (AppStorage.getBoolean(key)) return@launch
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
                    when (config.state) {
                        2 -> {
                            AppStorage.putBoolean(key, true)
                        }
                    }
                }
        }
    }

    suspend fun save(type: String, code: Int, name: String, message: String) {
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

    class StartUp : Initializer<Protector> {
        override fun create(context: Context): Protector {
            register()
            return Protector
        }

        override fun dependencies(): List<Class<out Initializer<*>>> {
            return listOf(ApplicationHolder.StartUp::class.java)
        }
    }
}