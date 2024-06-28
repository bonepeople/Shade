package androidx.shade.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.shade.Lighting
import androidx.shade.sample.databinding.ActivityMainBinding
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppView.singleClick
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        views.buttonSave.singleClick { save() }
    }

    private fun save() {
        CoroutinesHolder.io.launch {
            Lighting.c5("shade.sample.test", 1, "test", "test save function")
        }
    }
}