package androidx.shade.sample.module.home

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.shade.EarthTime
import androidx.shade.Lighting
import androidx.shade.Protector
import androidx.shade.sample.databinding.ActivityMainBinding
import androidx.shade.sample.module.home.text.HomeText
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.util.AppTime
import com.bonepeople.android.widget.util.AppView.singleClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : FragmentActivity() {
    private val views: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Protector.protect {
            setContentView(views.root)
            views.buttonSave.singleClick { save() }
        }
        updatePageText()
        startTimeUpdate()
    }

    private fun updatePageText() {
        val text: HomeText = StringResourceManager.get(HomeText.templateClass)
        views.buttonSave.text = text.saveLog
    }

    private fun startTimeUpdate() {
        lifecycleScope.launch {
            while (true) {
                views.textTime.text = AppTime.formatTime(EarthTime.now())
                delay(1000)
            }
        }
    }

    private fun save() {
        CoroutinesHolder.io.launch {
            Lighting.c5("shade.sample.test", 1, "test", "test save function")
        }
    }
}