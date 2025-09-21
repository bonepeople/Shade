package androidx.shade.sample.module.global.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity

open class BaseActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}