package androidx.shade.sample.module.start

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.shade.sample.databinding.ActivityStartBinding
import androidx.shade.sample.module.global.base.BaseActivity
import androidx.shade.sample.module.home.HomeActivity
import androidx.shade.sample.module.start.text.StartText
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.util.AppToast

class StartActivity : BaseActivity() {
    private val views: ActivityStartBinding by lazy { ActivityStartBinding.inflate(layoutInflater) }
    private val viewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setOnExitAnimationListener { splashScreenView -> splashScreenView.remove() }
        }
        super.onCreate(savedInstanceState)
        setContentView(views.root)
        updatePageText()
        viewModel.pageState.observeWithLifecycle(this) { pageState ->
            when (pageState) {
                StartViewModel.PageState.Init, StartViewModel.PageState.Loading -> Unit
                StartViewModel.PageState.Finish -> navigateToMain()
                StartViewModel.PageState.Error -> AppToast.show(StringResourceManager.get(StartText.templateClass).initError)
            }
        }
        viewModel.init()
    }

    private fun updatePageText() {
        val text: StartText = StringResourceManager.get(StartText.templateClass)
        views.imageLogo.contentDescription = text.appName
        views.textAppName.text = text.appName
        views.textTagline.text = text.tagline
        views.textVersion.text = text.versionFormat.format(ApplicationHolder.getVersionName())
    }

    private fun navigateToMain() {
        startActivity(Intent(this, HomeActivity::class.java))
        finishAfterTransition()
    }
}