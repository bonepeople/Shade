package androidx.shade.util

import com.bonepeople.android.widget.util.AppLog

internal object InternalLogUtil {
    private const val PREFIX = "Shade| "
    val logger = AppLog.tag("ShadeAppLog").apply { enable = MetaDataUtil.getBoolean("ShadeAppLog.enable") }

    fun verbose(msg: String) {
        logger.verbose("$PREFIX$msg")
    }

    fun info(msg: String) {
        logger.info("$PREFIX$msg")
    }

    fun debug(msg: String) {
        logger.debug("$PREFIX$msg")
    }

    fun warn(msg: String) {
        logger.warn("$PREFIX$msg")
    }

    fun error(msg: String) {
        logger.error("$PREFIX$msg")
    }
}