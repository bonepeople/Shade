package com.bonepeople.android.shade.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bonepeople.android.shade.Lighting
import com.bonepeople.android.shade.simple.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        views.buttonConfig.setOnClickListener { config() }
        views.buttonSave.setOnClickListener { save() }
    }

    private fun config() {
        Log.e("ShadeTag", "config")
        Lighting.fetchConfig()
    }

    private fun save() {
        Log.e("ShadeTag", "save")
        Lighting.save(1, 1, "test", "123")
    }
}