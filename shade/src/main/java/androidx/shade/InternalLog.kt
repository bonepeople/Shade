package androidx.shade

import com.bonepeople.android.widget.util.AppLog

internal object InternalLog {
    private val logger = AppLog.tag("ShadeAppLog").apply { enable = MetaDataUtil.getBoolean("ShadeAppLog.enable", false) }
    fun log(content: String) {
        logger.debug("Shade| $content")
    }
}