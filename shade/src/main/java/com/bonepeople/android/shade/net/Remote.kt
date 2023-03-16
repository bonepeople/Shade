package com.bonepeople.android.shade.net

import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppGson
import com.bonepeople.android.widget.util.AppRandom
import com.bonepeople.android.widget.util.AppStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

internal object Remote {
    private val appName by lazy {
        var name = AppStorage.getString("com.bonepeople.android.key.APP_NAME")
        if (name.isEmpty()) {
            name = ApplicationHolder.packageInfo.applicationInfo.loadLabel(ApplicationHolder.instance.packageManager).toString()
        }
        name
    }
    private const val publicKey = """
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtOQ2bW3rWdTuKXtc6yEzHNYKWcngICDj
    FvCZ4Slzym5SApnz4GOiyXKCAsuEy+gNK3VJioR2wTA6MLgXW+FdgzGOT+pgkRb0htJcrlTGer1K
    VVYTKG2ds8q7x8/cZbhVanluG9rksPQTnVKDLqlsbfrk1T2ZQUE8BVA2wuN8WsEcOzmMckH4/2Wi
    fhWknpDZzfGs2r0K/RWoOpjV38Z5xveM/RZ67zN8be6vxXaWiSLHImt5L1OxkkZCtjMzmIOqDJv5
    ixIObBr6pRCBBcy8hzj16mYQkvCa25fSn6R0Naru21OSZoYNbYN3txLul7JiqBfhPpx0zehUdHhP
    nONMoQIDAQAB
    """
    private val api: Api by lazy {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://www.google.com/")
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(Api::class.java)
    }
    private val passwords = HashMap<String, String>()
    private val encryptKey by lazy { AppEncrypt.decodeRSAPublicKey(publicKey) }

    private inline fun handleResponse(request: () -> String): Response {
        return kotlin.runCatching {
            request().let<String, Response> {
                val response = it.split(":")
                val id = response[0]
                val password = passwords[id] ?: ""
                val data = response[1]
                val json = AppEncrypt.decryptByAES(data, password.take(16), password.takeLast(16))
                AppGson.toObject(json)
            }
        }.getOrElse {
            Response().apply {
                code = if (it is CancellationException) Response.CANCEL else Response.FAILURE
                msg = it.message ?: ""
            }
        }
    }

    private fun generateBody(action: String, version: Int, data: Any? = null): RequestBody {
        val id = UUID.randomUUID().toString()
        val password = AppRandom.randomString(32)
        passwords[id] = password
        val map = HashMap<String, Any?>()
        map["action"] = action
        map["version"] = version
        map["debug"] = ApplicationHolder.debug
        map["password"] = password
        map["requestId"] = id
        data?.let { map["requestData"] = AppGson.toJson(it) }
        map["requestTime"] = System.currentTimeMillis()
        val encryptData = AppEncrypt.encryptByAES(AppGson.toJson(map), password.take(16), password.takeLast(16))
        return "${AppEncrypt.encryptByRSA(password, encryptKey)}:$encryptData".toRequestBody("text/plain".toMediaTypeOrNull())
    }

    suspend fun register(data: ConfigRequest) = handleResponse {
        api.post(generateBody("shade.register.$appName", 1, data))
    }

    suspend fun log(data: LogRequest) = handleResponse {
        api.post(generateBody("shade.log.$appName", 1, data))
    }
}