package androidx.shade.sample.module.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class StartViewModel : ViewModel() {
    val pageState: MutableStateFlow<PageState> = MutableStateFlow(PageState.Init)
    private val initialized = AtomicBoolean(false)

    fun init() {
        if (!initialized.compareAndSet(false, true)) return
        viewModelScope.launch {
            runCatching {
                pageState.value = PageState.Loading
                coroutineScope {
                    launch {
                        delay(2000)
                    }
                    launch {
                        delay(1000)
                    }
                }
                pageState.value = PageState.Finish
            }.onFailure {
                pageState.value = PageState.Error
            }
        }
    }

    sealed interface PageState {
        object Init : PageState
        object Loading : PageState
        object Finish : PageState
        object Error : PageState
    }
}