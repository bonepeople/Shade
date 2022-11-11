package com.bonepeople.android.shade.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bonepeople.android.shade.Protector
import com.bonepeople.android.shade.simple.databinding.ActivityMainBinding
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppToast
import kotlinx.coroutines.launch

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
        AppToast.show("App启动后会自动发送注册请求")
    }

    private fun save() {
        CoroutinesHolder.default.launch {
            Protector.save("shade.simple.test", 1, "test", "测试save函数")
        }
    }

    private fun generate() {
        AppToast.show("当前版本不需要生成配置信息")
    }
}