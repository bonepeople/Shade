package androidx.shade.net

import androidx.shade.EarthTime
import androidx.shade.Lighting
import androidx.shade.util.InternalLogUtil
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppData
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppGson
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal object DNSChecker {
    private const val TTL = 24 * 60 * 60 * 1000 // 24 hours
    private const val encryptHostUrl =
        "WxaK5NJaqRM/sEAZ56B8BQqnzZBefRIj2YtjfwABxwiSV4L1ATLlCTlfW2Njf8g/9jS6CuTsaKSbPOoX2lZd6TZtTRUvt/HjnPmWT5UbbS7MSmzTI8SvZDjrorILYt8D38whaEfJ+xrCIHpIY0huc0ZmpUyob74EJ4BIQlI/1eF0knJGPtxmVwVe/T/J4iJMjErIey+LSZ+Ydv9pomN7ZI6F50BLvKhGNlrcIqmmcgqtWOdFiJqxrEOuiKJX9IVVH/ZpYr5dRKSD2D4zBpr7wNr5POvdgE2P7ypV+6nf2T7P0DQinX5uGYJb9X9G/ALFn/d8aoVCdPKzjTRivMrpig=="
    private const val encryptConfigUrl =
        "ppBm6zvJjkexzE/X7vpdWcr0Cotgdhtja72uNKHlrb9J1CX/1pgvTglY6BUZ9TUfVLl4y2cZKjULWzAvkqSKRYiatnNnCtB2PaO5eFr9v2QYBJIeY8j2VnLNN5RgWuUD5fLCiUcWC9M+H30oaN1XGQt/srK/kgB1YKO/8QoeInLS/w2mt968piyraDx3S5tU2IhPS2p99wTMwcUjwqyacBP+dVaD2gQdLAxAr+/OZYuMKl6NSmQGIMsWq1iM/8YR6oI/LPSN8lcbPw376vgJkGOkm1NBA/S7Fr8dXFrkcE0AetCH2DDp+QRJ8utcy2BOlwdErdS12YZwBsYeDGpQ1A=="
    private val defaultHostUrl by lazy { AppEncrypt.decryptByRSA(encryptHostUrl, AppEncrypt.decodeRSAPublicKey(Remote.publicKey)) }
    private val configUrl by lazy { AppEncrypt.decryptByRSA(encryptConfigUrl, AppEncrypt.decodeRSAPublicKey(Remote.publicKey)) }
    private val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                preconfigured = provideUnsafeOkHttpClient()
            }
            defaultRequest { url("https://www.google.com/") }
            expectSuccess = true
        }
    }

    private fun provideUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun check() {
        CoroutinesHolder.io.launch {
            if (EarthTime.now() - AppData.create("com.android.shade.host").getLong("lastUpdateTime", 0) < TTL) return@launch
            InternalLogUtil.logger.verbose("Shade| update host")
            kotlin.runCatching {
                val encryptData = client.get(configUrl).bodyAsText()
                val json = AppEncrypt.decryptByRSA(encryptData, AppEncrypt.decodeRSAPublicKey(Remote.publicKey))
                val data: Map<String, String> = AppGson.toObject(json)
                val hostUrl = data[ApplicationHolder.getPackageName()] ?: data["default"] ?: defaultHostUrl
                AppData.create("com.android.shade.host").putLong("lastUpdateTime", EarthTime.now())
                AppData.create("com.android.shade.host").putString("url", hostUrl)
                InternalLogUtil.logger.verbose("Shade| update host: $hostUrl")
            }.getOrElse {
                Lighting.c5("shade.host", 1, "getHostError", it.message ?: "unknown")
                InternalLogUtil.logger.verbose("Shade| update host error: ${it.message}")
            }
        }
    }

    fun getHost(): String {
        return AppData.create("com.android.shade.host").getStringSync("url", defaultHostUrl)
    }
}