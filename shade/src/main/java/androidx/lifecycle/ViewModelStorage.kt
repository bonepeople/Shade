package androidx.lifecycle

/**
 * Associates extra data with a ViewModel via addCloseable / getCloseable.
 * getTag / setTagIfAbsent were removed in lifecycle-viewmodel 2.8.0.
 * https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.0
 */
@Suppress("Unused")
object ViewModelStorage {
    fun <T> ViewModel.getExtra(key: String): T? = getCloseable<ExtraHolder<T>>(key)?.value

    fun <T> ViewModel.putExtraIfAbsent(key: String, extra: () -> T): T {
        synchronized(this) {
            getCloseable<ExtraHolder<T>>(key)?.value?.let { return it }
            val value = extra()
            addCloseable(key, ExtraHolder(value))
            return value
        }
    }

    @Deprecated(message = "Use putExtraIfAbsent(key) { value } so the extra is created only when the key is absent.", replaceWith = ReplaceWith("putExtraIfAbsent(key) { value }"))
    fun <T> ViewModel.putExtraIfAbsent(key: String, value: T): T = putExtraIfAbsent(key) { value }

    private class ExtraHolder<T>(val value: T) : AutoCloseable {
        override fun close() {
            (value as? AutoCloseable)?.close()
        }
    }
}