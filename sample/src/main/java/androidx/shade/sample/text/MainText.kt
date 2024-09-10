package androidx.shade.sample.text

import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.resource.StringTemplate
import java.util.Locale

abstract class MainText : StringTemplate {
    override val templateClass: Class<out StringTemplate> = Companion.templateClass

    abstract val saveLog: String

    companion object {
        val templateClass: Class<MainText> = MainText::class.java

        init {
            StringResourceManager.register(MainTextEnUS(), Locale.ENGLISH)
            StringResourceManager.register(MainTextZhCN(), Locale.SIMPLIFIED_CHINESE)
        }
    }
}