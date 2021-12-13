package com.bonepeople.android.shade.global

import android.app.Application

interface AppInformation {
    val debugMode: Boolean
    val versionCode: Int
    val versionName: String
    val logName: String
    val secret: String
    val salt: String

    fun getApplication(): Application
    fun getUserId(): String
}