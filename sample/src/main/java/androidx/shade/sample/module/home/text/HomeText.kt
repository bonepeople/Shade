package androidx.shade.sample.module.home.text

import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.resource.StringTemplate
import java.util.Locale

abstract class HomeText : StringTemplate {
    override val templateClass: Class<out StringTemplate> = Companion.templateClass

    abstract val saveLog: String

    companion object {
        val templateClass: Class<HomeText> = HomeText::class.java

        init {
            StringResourceManager.register(HomeTextEnUS(), Locale.ENGLISH)
            StringResourceManager.register(HomeTextZhCN(), Locale.SIMPLIFIED_CHINESE)
        }
    }
}