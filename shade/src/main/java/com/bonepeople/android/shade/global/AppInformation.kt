package com.bonepeople.android.shade.global

interface AppInformation {
    val debugMode: Boolean
    val appSecret: String

    fun getUserId(): String
}