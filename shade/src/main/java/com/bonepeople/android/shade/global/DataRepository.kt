package com.bonepeople.android.shade.global

import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.data.HttpResponse
import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.shade.util.AESUtil
import com.bonepeople.android.shade.util.GsonUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

object DataRepository {
    private const val BASE = "http://bonepeople.tpddns.cn:8192/"
    private val api: NetApi by lazy {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetApi::class.java)
    }

    private inline fun <R> handleResponse(request: () -> HttpResponse<R>): HttpResponse<R> {
        return kotlin.runCatching {
            request()
        }.getOrElse {
            HttpResponse<R>().apply {
                code = if (it is CancellationException) HttpResponse.CANCEL else HttpResponse.FAILURE
                msg = it.message ?: ""
            }
        }
    }

    private fun generateBody(data: Any? = null): RequestBody {
        val json = GsonUtil.toJson(data)
        val encrypt = AESUtil.encrypt(json)
        return encrypt.toRequestBody("application/json".toMediaTypeOrNull())
    }

    suspend fun getConfig(data: ConfigRequest): HttpResponse<String> {
        return handleResponse {
            val url = "${BASE}app/${Lighting.appInformation.logName}/config"
            api.post(url, generateBody(data))
        }
    }

    suspend fun log(data: LogRequest): HttpResponse<String> {
        return handleResponse {
            val url = "${BASE}app/${Lighting.appInformation.logName}/log"
            api.post(url, generateBody(data))
        }
    }
}