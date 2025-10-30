package androidx.shade.sample.module.start.text

import com.bonepeople.android.widget.resource.StringResourceManager
import com.bonepeople.android.widget.resource.StringTemplate
import java.util.Locale

abstract class StartText : StringTemplate {
    override val templateClass: Class<out StringTemplate> = Companion.templateClass

    abstract val appName: String
    abstract val tagline: String
    abstract val versionFormat: String
    abstract val initError: String

    companion object {
        val templateClass: Class<StartText> = StartText::class.java

        init {
            StringResourceManager.register(StartTextEnUS(), Locale.ENGLISH)
            StringResourceManager.register(StartTextZhCN(), Locale.SIMPLIFIED_CHINESE)
        }
    }
}