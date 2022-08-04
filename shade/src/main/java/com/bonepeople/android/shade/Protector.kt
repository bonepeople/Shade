package com.bonepeople.android.shade

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.startup.Initializer
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Protector {

    @SuppressLint("PackageManagerGetSignatures")
    private fun register() {
        CoroutinesHolder.default.launch {
            delay(2000)
            val info = ConfigRequest().apply {
                userId = AppStorage.getString("USER_ID")
                androidId = Settings.System.getString(ApplicationHolder.instance.contentResolver, Settings.Secure.ANDROID_ID)
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
        }
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