package com.bonepeople.android.shade.net

import com.bonepeople.android.shade.EarthTime
import com.bonepeople.android.shade.data.ConfigRequest
import com.bonepeople.android.shade.data.LogRequest
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppGson
import com.bonepeople.android.widget.util.AppMessageDigest
import com.bonepeople.android.widget.util.AppRandom
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
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
    private val encryptKey by lazy { AppEncrypt.decodeRSAPublicKey(publicKey) }
    private val client: HttpClient by lazy {
        HttpClient(Android) {
            engine {
                connectTimeout = 10_000
                socketTimeout = 10_000
            }
            expectSuccess = true
        }
    }

    private suspend fun requestApi(action: String, version: Int, data: Any? = null): Response {
        return kotlin.runCatching {
            val password = AppRandom.randomString(32)
            client.post("http://bonepeople.tpddns.cn:8192/open") {
                val map = HashMap<String, Any?>()
                map["action"] = action
                map["version"] = version
                map["debug"] = ApplicationHolder.debug
                map["password"] = password
                data?.let { map["requestData"] = AppGson.toJson(it) }
                map["requestTime"] = EarthTime.now()
                val encryptData = AppEncrypt.encryptByAES(AppGson.toJson(map), password.take(16), password.takeLast(16))
                val header = HashMap<String, Any?>()
                header["password"] = password
                header["md5"] = AppMessageDigest.md5(encryptData)
                val encryptHeader = AppEncrypt.encryptByRSA(AppGson.toJson(header), encryptKey)
                setBody("$encryptHeader:$encryptData")
            }.body<String>().split(":").let<List<String>, Response> { response ->
                val json = AppEncrypt.decryptByAES(response[1], password.take(16), password.takeLast(16))
                AppGson.toObject(json)
            }
        }.getOrElse {
            Response().apply {
                code = if (it is CancellationException) Response.CANCEL else Response.FAILURE
                msg = it.message ?: ""
            }
        }
    }

    suspend fun register(data: ConfigRequest) = requestApi("shade.register.${data.appName}", 1, data)

    suspend fun log(data: LogRequest) = requestApi("shade.log.${data.appName}", 1, data)
}