package androidx.shade.sample

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.shade.sample.databinding.ActivityStartBinding
import com.bonepeople.android.widget.ApplicationHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartActivity : FragmentActivity() {
    private val views: ActivityStartBinding by lazy { ActivityStartBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContentView(views.root)
        ViewCompat.setOnApplyWindowInsetsListener(views.root) { view, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom)
            insets
        }
        views.textVersion.text = getString(R.string.start_version_format, ApplicationHolder.getVersionName())
        lifecycleScope.launch {
            delay(2000)
            startActivity(Intent(this@StartActivity, MainActivity::class.java))
            finish()
        }
    }
}