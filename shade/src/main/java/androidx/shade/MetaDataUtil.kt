package androidx.shade

import android.content.pm.PackageManager
import com.bonepeople.android.widget.ApplicationHolder

object MetaDataUtil {
    fun getValue(key: String): String? {
        return kotlin.runCatching {
            val applicationInfo = ApplicationHolder.app.packageManager.getApplicationInfo(ApplicationHolder.app.packageName, PackageManager.GET_META_DATA)
            applicationInfo.metaData?.getString(key)
        }.getOrElse {
            null
        }
    }
}