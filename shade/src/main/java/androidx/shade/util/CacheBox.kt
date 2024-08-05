package androidx.shade.util

import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppGson
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal object CacheBox {
    private val box: ConcurrentHashMap<String, String> by lazy { init() }
    private val updateFile: MutableSharedFlow<Int> = MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)

    private fun init(): ConcurrentHashMap<String, String> {
        val data = ConcurrentHashMap<String, String>()
        kotlin.runCatching {
            val parentFile = ApplicationHolder.app.filesDir
            val json = File(parentFile, "cache.db").readText().let { AppEncrypt.decryptByAES(it, "CacheBox", "bone") }
            val map = AppGson.toObject<Map<String, String>>(json)
            data.putAll(map)
        }
        updateFile.onEach { save() }.launchIn(CoroutinesHolder.io)
        return data
    }

    private suspend fun save() {
        kotlin.runCatching {
            val json = AppGson.toJson(box)
            val parentFile = ApplicationHolder.app.filesDir
            File(parentFile, "cache.db").writeText(AppEncrypt.encryptByAES(json, "CacheBox", "bone"))
        }
    }

    fun putString(key: String, value: String) {
        box[key] = value
        updateFile.tryEmit(1)
    }

    fun getString(key: String, default: String): String {
        return box[key] ?: default
    }

    fun putLong(key: String, value: Long) {
        box[key] = value.toString()
        updateFile.tryEmit(1)
    }

    fun getLong(key: String, default: Long): Long {
        return kotlin.runCatching {
            box[key]!!.toLong()
        }.getOrDefault(default)
    }
}