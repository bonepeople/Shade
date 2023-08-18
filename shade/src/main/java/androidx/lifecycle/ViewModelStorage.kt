package androidx.lifecycle

@Suppress("unused")
object ViewModelStorage {
    fun <T> ViewModel.getExtra(key: String): T? = getTag(key)

    fun <T> ViewModel.putExtraIfAbsent(key: String, value: T): T = setTagIfAbsent(key, value)
}