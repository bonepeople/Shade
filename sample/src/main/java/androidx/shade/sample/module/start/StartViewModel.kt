package androidx.shade.sample.module.start

import com.bonepeople.android.base.foundation.QuickViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StartViewModel : QuickViewModel() {
    val pageState: MutableStateFlow<PageState> = MutableStateFlow(PageState.Init)

    fun init() = initialize {
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

    sealed interface PageState {
        object Init : PageState
        object Loading : PageState
        object Finish : PageState
        object Error : PageState
    }
}