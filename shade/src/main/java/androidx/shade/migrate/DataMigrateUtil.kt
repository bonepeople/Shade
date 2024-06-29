package androidx.shade.migrate

import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object DataMigrateUtil {
    private val migrateMutex = Mutex()

    suspend fun migrate(dataId: String, migrateList: List<DataMigrateInfo>): Int {
        return withContext(Dispatchers.IO) {
            return@withContext migrateMutex.withLock {
                AppLog.defaultLog.verbose("migrate $dataId")
                var currentVersion = getVersion(dataId)
                AppLog.defaultLog.info("current version: $currentVersion")
                if (migrateList.none { it.range.first == currentVersion }) return@withLock currentVersion

                AppLog.defaultLog.info("migrate list: $migrateList")
                val sortedList = migrateList.sortedWith { o1, o2 ->
                    if (o1.range.first == o2.range.first) {
                        o2.range.last.compareTo(o1.range.last)
                    } else {
                        o1.range.first.compareTo(o2.range.first)
                    }
                }
                AppLog.defaultLog.info("sorted list: $sortedList")
                sortedList.forEach {
                    AppLog.defaultLog.info("check migrate: $it")
                    if (!it.range.isEmpty() && it.range.first == currentVersion) {
                        AppLog.defaultLog.info("migrate: $it")
                        it.action()
                        AppLog.defaultLog.info("update version: ${it.range.last}")
                        currentVersion = it.range.last
                        setVersion(dataId, currentVersion)
                    }
                }
                return@withLock currentVersion
            }
        }
    }

    //packageName\data\versions\dataId\summary => 1\2\3\4
    //packageName\data\versions\main => 1\2\3\4
    private fun getVersion(dataId: String): Int {
        return kotlin.runCatching {
            val parentFile = ApplicationHolder.app.filesDir.resolve("versions").resolve(dataId)
            val summary = parentFile.resolve("summary")
            summary.readText().toInt()
        }.getOrElse {
            AppLog.defaultLog.error("get version error: $it")
            0
        }
    }

    private fun setVersion(dataId: String, version: Int) {
        kotlin.runCatching {
            val parentFile = ApplicationHolder.app.filesDir.resolve("versions").resolve(dataId)
            parentFile.mkdirs()
            val summary = parentFile.resolve("summary")
            summary.writeText(version.toString())
        }.getOrElse {
            AppLog.defaultLog.error("set version error: $it")
        }
    }
}