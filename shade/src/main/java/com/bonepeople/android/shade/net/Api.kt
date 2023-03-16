package com.bonepeople.android.shade.net

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {

    @POST("http://bonepeople.tpddns.cn:8192/light")
    suspend fun post(@Body requestBody: RequestBody): String
}