package androidx.shade

import android.content.pm.PackageManager
import com.bonepeople.android.widget.ApplicationHolder

object MetaDataUtil {
    fun getBoolean(key: String, default: Boolean): Boolean {
        return kotlin.runCatching {
            val applicationInfo = ApplicationHolder.app.packageManager.getApplicationInfo(ApplicationHolder.app.packageName, PackageManager.GET_META_DATA)
            applicationInfo.metaData.getBoolean(key, default)
        }.getOrElse {
            default
        }
    }
}