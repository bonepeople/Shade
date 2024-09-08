package androidx.shade.strings

import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.resource.StringTemplate
import java.util.Locale

internal abstract class ShadeString : StringTemplate {
    override val templateClass: Class<out StringTemplate> = Companion.templateClass
    abstract val unAuthorized: String
    abstract val illegal: String

    companion object {
        val templateClass: Class<ShadeString> = ShadeString::class.java

        init {
            StringResourceManager.register(ShadeStringEnUS(), Locale.ENGLISH)
            StringResourceManager.register(ShadeStringZhCN(), Locale.SIMPLIFIED_CHINESE)
        }
    }
}