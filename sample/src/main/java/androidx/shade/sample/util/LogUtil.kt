package androidx.shade.sample.util

import com.bonepeople.android.widget.util.AppLog

object LogUtil {
    val app = AppLog.tag("AppLog.Shade.App")
    val test = AppLog.tag("AppLog.Shade.Test").apply { showStackInfo = true;showThreadInfo = true }
}