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
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

internal object Remote {
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
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }
    private val password by lazy { AppRandom.randomString(32) }
    private val encryptKey by lazy { AppEncrypt.decodeRSAPublicKey(publicKey) }

    private inline fun handleResponse(request: () -> Response): Response {
        return kotlin.runCatching {
            request().also {
                if (it.code == Response.SUCCESSFUL) {
                    it.data = AppEncrypt.decryptByAES(it.data, password.substring(0, 16), password.substring(16, 32))
                }
            }
        }.getOrElse {
            Response().apply {
                code = if (it is CancellationException) Response.CANCEL else Response.FAILURE
                msg = it.message ?: ""
            }
        }
    }

    private fun generateBody(action: String, version: Int, data: Any? = null): RequestBody {
        val map = HashMap<String, Any?>()
        map["action"] = action
        map["version"] = version
        map["debug"] = ApplicationHolder.debug
        map["password"] = password
        map["time"] = System.currentTimeMillis()
        data?.let { map["data"] = AppGson.toJson(it) }
        val encrypt = AppEncrypt.encryptByRSA(AppGson.toJson(map), encryptKey)
        return encrypt.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    suspend fun register(data: ConfigRequest) = handleResponse {
        val app = AppStorage.getString("com.bonepeople.android.key.APP_NAME")
        api.post(generateBody("shade.register.$app", 1, data))
    }

    suspend fun log(data: LogRequest) = handleResponse {
        val app = AppStorage.getString("com.bonepeople.android.key.APP_NAME")
        api.post(generateBody("shade.log.$app", 1, data))
    }
}