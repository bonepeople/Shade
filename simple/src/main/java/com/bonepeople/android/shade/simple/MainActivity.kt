package com.bonepeople.android.shade.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.simple.databinding.ActivityMainBinding
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
            Lighting.c5("shade.simple.test", 1, "test", "测试save函数")
        }
    }
}