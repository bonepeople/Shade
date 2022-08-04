package com.bonepeople.android.shade.simple

import android.app.Application
import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.global.AppInformation

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Lighting.appInformation = object : AppInformation {
            override val debugMode = false
            override val appSecret =
                "zKp9wl/hpya+1NVGpQSTuvtRAo0EZNuxW7mt7ZrLacntTLh4JLtVSeA3ZmvGgIiTMSwzYQZt5mPqx4AmexvqiXQ3EIC9CmA4EMlDUk1wfZNNNjzt5g+DjsBJtN1YZmKwQufKDOUa+hZz7WtTuD70pDmeeILVtOJHg0ceXF5ZvpotWzylGmNmijstE1CVL9fBFUB6MK7+mY0IuuuV1/yJbQ=="

            override fun getUserId() = "1"
        }
    }
}