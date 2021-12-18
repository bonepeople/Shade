package com.bonepeople.android.shade.simple

import android.app.Application
import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.global.AppInformation

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Lighting.appInformation = object : AppInformation {
            override val debugMode = false
            override val versionCode = BuildConfig.VERSION_CODE
            override val versionName = BuildConfig.VERSION_NAME
            override val logName = "test"
            override val secret = "secret----------"
            override val salt = "salt------------"
            override fun getApplication() = this@App
            override fun getUserId() = "1"
        }
    }
}