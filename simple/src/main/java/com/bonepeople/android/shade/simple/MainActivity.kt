package com.bonepeople.android.shade.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.simple.databinding.ActivityMainBinding
import com.bonepeople.android.widget.util.AppLog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        views.buttonConfig.setOnClickListener { config() }
        views.buttonSave.setOnClickListener { save() }
    }

    private fun config() {
        AppLog.debug("config")
        Lighting.fetchConfig()
    }

    private fun save() {
        AppLog.debug("save")
        Lighting.save(1, 1, "test", "123")
    }
}