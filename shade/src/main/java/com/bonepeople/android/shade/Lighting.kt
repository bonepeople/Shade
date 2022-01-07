package com.bonepeople.android.shade

import android.os.Build
import android.provider.Settings
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.shade.data.Environment
import com.bonepeople.android.shade.data.ShadeConfig
import com.bonepeople.android.shade.global.AppInformation
import com.bonepeople.android.shade.global.DataRepository
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppGson
import com.bonepeople.android.widget.util.AppStorage
import kotlinx.coroutines.launch

object Lighting {
    lateinit var appInformation: AppInformation
    internal val config: ShadeConfig by lazy {
        val secret = AppEncrypt.encryptByMD5(ApplicationHolder.getPackageName())
        val data = AppEncrypt.decryptByAES(appInformation.appSecret, secret.substring(0, 16), secret.substring(16, 32))
        AppGson.toObject(data)
    }
    private var environment: Environment = AppGson.toObject("{}")

    fun fetchConfig() {
        if (appInformation.debugMode) {
            return
        }
        val info = ConfigRequest().apply {
            userId = appInformation.getUserId()
            androidId = Settings.System.getString(ApplicationHolder.instance.contentResolver, Settings.Secure.ANDROID_ID)
            systemVersion = Build.VERSION.SDK_INT
            deviceModel = Build.MODEL
            deviceManufacturer = Build.MANUFACTURER
            versionCode = ApplicationHolder.getVersionCode()
            versionName = ApplicationHolder.getVersionName()
            installTime = ApplicationHolder.packageInfo.firstInstallTime
            updateTime = System.currentTimeMillis()
        }

        CoroutinesHolder.default.launch {
            DataRepository.getConfig(info)
                .onSuccess {
                    val json = AppEncrypt.decryptByAES(it, config.secret, config.salt)
                    AppStorage.putString("ShadeConfig", json)
                    environment = AppGson.toObject(json)
                }
                .onFailure { _, _ ->
                    val json = AppStorage.getString("ShadeConfig", "{}")
                    environment = AppGson.toObject(json)
                }
        }
    }

    fun save(flow: Int, code: Int, name: String, message: String) {
        if (appInformation.debugMode || !environment.needUpload) {
            return
        }
        val info = LogRequest().apply {
            userId = appInformation.getUserId()
            androidId = Settings.System.getString(ApplicationHolder.instance.contentResolver, Settings.Secure.ANDROID_ID)
            this.flow = flow
            this.code = code
            this.name = name
            this.message = message
            time = System.currentTimeMillis()
        }

        CoroutinesHolder.default.launch {
            DataRepository.log(info)
        }
    }
}