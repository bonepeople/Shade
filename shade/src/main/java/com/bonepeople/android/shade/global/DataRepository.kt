package com.bonepeople.android.shade.global

import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.data.HttpResponse
import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppGson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

internal object DataRepository {
    private const val BASE = "https://www.baidu.com/"
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
        val stringBuilder = StringBuilder()
        stringBuilder.append("I%6peT")
        val map = HashMap<String, Any?>()
        map["version"] = 1
        map["time"] = System.currentTimeMillis()
        data?.let { map["data"] = AppGson.toJson(it) }
        val json = AppGson.toJson(map)
        val encrypt = AppEncrypt.encryptByAES(json, "vH6mR9rw2qXTlv2J", "jUE5HeW12q33uMqx")
        stringBuilder.append(encrypt)
        return stringBuilder.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }

    suspend fun getConfig(data: ConfigRequest): HttpResponse<String> {
        return handleResponse {
            val url = "${Lighting.config.path}/config"
            api.post(url, generateBody(data))
        }
    }

    suspend fun log(data: LogRequest): HttpResponse<String> {
        return handleResponse {
            val url = "${Lighting.config.path}/log"
            api.post(url, generateBody(data))
        }
    }
}