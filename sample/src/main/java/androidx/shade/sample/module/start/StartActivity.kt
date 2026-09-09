package androidx.shade.sample.module.start

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.shade.sample.MainActivity
import androidx.shade.sample.R
import androidx.shade.sample.databinding.ActivityStartBinding
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.util.AppToast
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class StartActivity : FragmentActivity() {
    private val views: ActivityStartBinding by lazy { ActivityStartBinding.inflate(layoutInflater) }
    private val viewModel: StartViewModel by viewModels()

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
            viewModel.pageState.flowWithLifecycle(lifecycle).distinctUntilChanged().collect { pageState ->
                when (pageState) {
                    StartViewModel.PageState.Init, StartViewModel.PageState.Loading -> Unit
                    StartViewModel.PageState.Finish -> navigateToMain()
                    StartViewModel.PageState.Error -> AppToast.show(getString(R.string.start_init_error))
                }
            }
        }
        viewModel.init()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }
}