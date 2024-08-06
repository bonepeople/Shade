package androidx.shade.util

import com.bonepeople.android.widget.util.AppLog

internal object InternalLogUtil {
    val logger = AppLog.tag("ShadeAppLog").apply { enable = MetaDataUtil.getBoolean("ShadeAppLog.enable") }
}