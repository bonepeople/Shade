package com.bonepeople.android.shade.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bonepeople.android.shade.Protector
import com.bonepeople.android.shade.simple.databinding.ActivityMainBinding
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.AppToast
import com.bonepeople.android.widget.util.AppView.singleClick
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        views.buttonConfig.singleClick { config() }
        views.buttonSave.singleClick { save() }
        views.buttonGenerate.singleClick { generate() }
    }

    private fun config() {
        AppToast.show("App启动后会自动发送注册请求")
    }

    private fun save() {
        CoroutinesHolder.io.launch {
            Protector.c5("shade.simple.test", 1, "test", "测试save函数")
        }
    }

    private fun generate() {
        Protector.protect {
            AppToast.show("当前版本不需要生成配置信息")
        }
    }
}