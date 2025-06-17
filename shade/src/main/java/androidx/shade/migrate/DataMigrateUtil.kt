package androidx.shade.migrate

import com.bonepeople.android.widget.ApplicationHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("Unused")
object DataMigrateUtil {
    private val mutexMap: MutableMap<String, Mutex> = mutableMapOf()

    suspend fun migrate(dataId: String, migrateList: List<DataMigrateInfo>): Int {
        return withContext(Dispatchers.IO) {
            if (!checkValidName(dataId)) {
                throw IllegalArgumentException("Invalid name: $dataId")
            }
            val migrateMutex = synchronized(DataMigrateUtil) { mutexMap.getOrPut(dataId) { Mutex() } }
            return@withContext migrateMutex.withLock {
                var currentVersion = getVersion(dataId)
                if (migrateList.none { it.range.first == currentVersion }) return@withLock currentVersion

                val sortedList = migrateList.sortedWith { o1, o2 ->
                    if (o1.range.first == o2.range.first) {
                        o2.range.last.compareTo(o1.range.last)
                    } else {
                        o1.range.first.compareTo(o2.range.first)
                    }
                }
                sortedList.forEach {
                    if (!it.range.isEmpty() && it.range.first == currentVersion) {
                        it.action()
                        currentVersion = it.range.last
                        setVersion(dataId, currentVersion)
                    }
                }
                return@withLock currentVersion
            }
        }
    }

    private fun checkValidName(name: String): Boolean {
        // 1. Check if the name is empty or exceeds the length limit
        if (name.isBlank() || name.length > 255) {
            return false
        }
        // 2. Check for illegal characters
        val illegalChars = listOf('<', '>', ':', '"', '/', '\\', '|', '?', '*')
        if (name.any { it in illegalChars }) {
            return false
        }
        // 3. Check if the name contains invisible characters (e.g., control characters)
        if (name.any { it.isISOControl() }) {
            return false
        }
        // 4. Check if the name ends with illegal characters (such as "." or " ")
        if (name.endsWith(".") || name.endsWith(" ")) {
            return false
        }
        return true
    }

    //packageName\data\versions\dataId\summary => 1\2\3\4
    //packageName\data\versions\main => 1\2\3\4
    private fun getVersion(dataId: String): Int {
        var retryTimes = 0
        var version = 0
        while (retryTimes < 3) {
            kotlin.runCatching {
                val parentFile: File = ApplicationHolder.app.filesDir.resolve("versions").resolve(dataId)
                val summary: File = parentFile.resolve("summary")
                version = summary.readText().toInt()
                retryTimes = 3
            }.getOrElse {
                retryTimes++
            }
        }
        return version
    }

    private fun setVersion(dataId: String, version: Int) {
        var retryTimes = 0
        while (retryTimes < 3) {
            kotlin.runCatching {
                val parentFile: File = ApplicationHolder.app.filesDir.resolve("versions").resolve(dataId)
                parentFile.mkdirs()
                val summary: File = parentFile.resolve("summary")
                summary.writeText(version.toString())
                retryTimes = 3
            }.getOrElse {
                retryTimes++
            }
        }
    }
}