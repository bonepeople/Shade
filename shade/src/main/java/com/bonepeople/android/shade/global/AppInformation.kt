package com.bonepeople.android.shade.global

interface AppInformation {
    val debugMode: Boolean
    val logName: String
    val secret: String
    val salt: String

    fun getUserId(): String
}