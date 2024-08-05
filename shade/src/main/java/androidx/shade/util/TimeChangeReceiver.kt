package androidx.shade.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.shade.EarthTime

internal class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        EarthTime.now()
    }
}