package com.bonepeople.android.shade

import android.content.Context
import androidx.startup.Initializer
import com.bonepeople.android.widget.ApplicationHolder

object Protector {

    class StartUp : Initializer<Protector> {
        override fun create(context: Context): Protector {
            return Protector
        }

        override fun dependencies(): List<Class<out Initializer<*>>> {
            return listOf(ApplicationHolder.StartUp::class.java)
        }
    }
}