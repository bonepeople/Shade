package androidx.shade.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.shade.Lighting
import com.bonepeople.android.base.manager.UserStateEvent
import com.bonepeople.android.widget.CoroutinesHolder
import com.bonepeople.android.widget.util.event.AppEvent
import kotlinx.coroutines.launch

internal object UserStateObserver {
    private val eventOwner = object : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle = registry

        fun resume() {
            if (registry.currentState == Lifecycle.State.INITIALIZED) {
                registry.currentState = Lifecycle.State.RESUMED
            }
        }
    }

    fun register() {
        eventOwner.resume()
        AppEvent.register(eventOwner) { event ->
            if (event is UserStateEvent) {
                when (event) {
                    UserStateEvent.LOGIN -> CoroutinesHolder.io.launch { Lighting.c5("shade.user", 1, "login", "") }
                    UserStateEvent.LOGOUT -> CoroutinesHolder.io.launch { Lighting.c5("shade.user", 2, "logout", "") }
                    UserStateEvent.UPDATE -> CoroutinesHolder.io.launch { Lighting.c5("shade.user", 3, "update", "") }
                }
            }
        }
    }
}