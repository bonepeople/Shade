package androidx.shade

import com.bonepeople.android.widget.util.AppLog

internal object InternalLog {
    private val logger = AppLog.tag("ShadeAppLog").apply { enable = MetaDataUtil.getValue("ShadeAppLog.enable").toBoolean() }
    fun log(content: String) {
        logger.debug("Shade| $content")
    }
}