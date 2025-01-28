package androidx.shade.internal.time

import androidx.shade.migrate.DataMigrateInfo
import androidx.shade.migrate.DataMigrateUtil
import androidx.shade.util.CacheBox
import com.bonepeople.android.widget.util.AppGson

internal object EarthTimeDataMigrate {
    private const val TIME_OFFSET = "androidx.shade.EarthTime.offset"
    private const val TIME_LAST = "androidx.shade.EarthTime.lastTime"
    private const val TIME_LOCAL = "androidx.shade.EarthTime.localTime"

    suspend fun migrateData() {
        DataMigrateUtil.migrate(
            dataId = EarthTimeEngine.TIME_SNAPSHOT,
            migrateList = listOf(MigrateInfo1),
        )
    }

    private object MigrateInfo1 : DataMigrateInfo {
        override val range: IntRange = 0..1
        override val action: suspend () -> Unit = {
            val snapshot = TimeSnapshot(
                deviceWallClockAtSyncMillis = CacheBox.getLong(TIME_LOCAL, 0),
                elapsedRealtimeAtSyncMillis = CacheBox.getLong(TIME_LAST, 0),
                networkTimeOffsetMillis = CacheBox.getLong(TIME_OFFSET, 0),
            )
            CacheBox.putString(EarthTimeEngine.TIME_SNAPSHOT, AppGson.toJson(snapshot))
        }
    }
}