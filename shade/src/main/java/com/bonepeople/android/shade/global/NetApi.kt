package com.bonepeople.android.shade.global

import com.bonepeople.android.shade.data.HttpResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface NetApi {

    @POST
    suspend fun post(@Url url: String, @Body requestBody: RequestBody): HttpResponse<String>
}