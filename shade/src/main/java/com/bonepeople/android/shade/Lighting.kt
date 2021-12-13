package com.bonepeople.android.shade

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.shade.data.ShadeConfig
import com.bonepeople.android.shade.global.AppInformation
import com.bonepeople.android.shade.global.DataRepository
import com.bonepeople.android.shade.util.AESUtil
import com.bonepeople.android.shade.util.GsonUtil
import com.bonepeople.android.shade.util.MMKVUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Lighting {
    lateinit var appInformation: AppInformation
    private var config: ShadeConfig = GsonUtil.toObject("{}")

    fun fetchConfig() {
        if (appInformation.debugMode) {
            return
        }
        val info = ConfigRequest().apply {
            userId = appInformation.getUserId()
            androidId = Settings.System.getString(appInformation.getApplication().contentResolver, Settings.Secure.ANDROID_ID)
            systemVersion = Build.VERSION.SDK_INT
            deviceModel = Build.MODEL
            deviceManufacturer = Build.MANUFACTURER
            versionCode = appInformation.versionCode
            versionName = appInformation.versionName
            var install: Long = 0
            kotlin.runCatching {
                install = appInformation.getApplication().packageManager.getPackageInfo(appInformation.getApplication().packageName, PackageManager.GET_INSTRUMENTATION).firstInstallTime
            }
            installTime = install
            updateTime = System.currentTimeMillis()
        }

        GlobalScope.launch {
            DataRepository.getConfig(info)
                .onSuccess {
                    val json = AESUtil.decrypt(it)
                    MMKVUtil.putString("ShadeConfig", json)
                    config = GsonUtil.toObject(json)
                }
                .onFailure { _, _ ->
                    val json = MMKVUtil.getString("ShadeConfig", "{}")
                    config = GsonUtil.toObject(json)
                }
        }
    }

    fun save(flow: Int, code: Int, name: String, message: String) {
        if (appInformation.debugMode || !config.needUpload) {
            return
        }
        val info = LogRequest().apply {
            userId = appInformation.getUserId()
            androidId = Settings.System.getString(appInformation.getApplication().contentResolver, Settings.Secure.ANDROID_ID)
            this.flow = flow
            this.code = code
            this.name = name
            this.message = message
            time = System.currentTimeMillis()
        }

        GlobalScope.launch {
            DataRepository.log(info)
        }
    }
}