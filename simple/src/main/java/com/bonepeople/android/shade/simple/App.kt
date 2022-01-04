package com.bonepeople.android.shade.simple

import android.app.Application
import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.global.AppInformation
import com.bonepeople.android.widget.util.AppLog

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLog.debug = true
        AppLog.tag = "ShadeTag"
        Lighting.appInformation = object : AppInformation {
            override val debugMode = true
            override val logName = "test"
            override val secret = "secret----------"
            override val salt = "salt------------"
            override fun getUserId() = "1"
        }
    }
}