package androidx.shade

import com.bonepeople.android.widget.util.AppLog

internal object InternalLog {
    private val logger = AppLog.tag("ShadeAppLog").apply { enable = false }
    fun log(content: String) {
        logger.debug("Shade| $content")
    }
}