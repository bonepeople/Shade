package androidx.shade.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.shade.Lighting
import androidx.shade.Protector
import androidx.shade.sample.databinding.ActivityMainBinding
import androidx.shade.sample.text.MainText
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.util.AppView.singleClick
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val views: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Protector.protect {
            setContentView(views.root)
            views.buttonSave.singleClick { save() }
        }
        updatePageText()
    }

    private fun updatePageText() {
        val text: MainText = StringResourceManager.get(MainText.templateClass)
        views.buttonSave.text = text.saveLog
    }

    private fun save() {
        CoroutinesHolder.io.launch {
            Lighting.c5("shade.sample.test", 1, "test", "test save function")
        }
    }
}