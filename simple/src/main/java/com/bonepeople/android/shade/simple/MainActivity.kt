package com.bonepeople.android.shade.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bonepeople.android.shade.simple.databinding.ActivityMainBinding
import com.bonepeople.android.widget.util.AppEncrypt
import com.bonepeople.android.widget.util.AppLog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        views.buttonConfig.setOnClickListener { config() }
        views.buttonSave.setOnClickListener { save() }
        views.buttonGenerate.setOnClickListener { generate() }
    }

    private fun config() {
        AppLog.debug("config")
    }

    private fun save() {
        AppLog.debug("save")
    }

    private fun generate() {
        val packageName = "com.bonepeople.android.shade.simple"
        val path = "http://192.168.1.200:8192/app/karo"
        val secret = "vH6mR9rw2qXTlv2J"
        val salt = "jUE5HeW12q33uMqx"

        val content = """{"path":"$path","secret":"$secret","salt":"$salt"}"""
        val md5 = AppEncrypt.encryptByMD5(packageName)
        val encrypt = AppEncrypt.encryptByAES(content, md5.substring(0, 16), md5.substring(16, 32))
        AppLog.debug("encrypt =\n$encrypt")

        val decrypt = AppEncrypt.decryptByAES(encrypt, md5.substring(0, 16), md5.substring(16, 32))
        AppLog.debug(decrypt)
    }
}