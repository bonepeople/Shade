package androidx.shade.util

import android.content.pm.PackageManager
import com.bonepeople.android.widget.ApplicationHolder

internal object MetaDataUtil {
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return kotlin.runCatching {
            val applicationInfo = ApplicationHolder.app.packageManager.getApplicationInfo(ApplicationHolder.app.packageName, PackageManager.GET_META_DATA)
            applicationInfo.metaData.getBoolean(key, default)
        }.getOrDefault(default)
    }
}