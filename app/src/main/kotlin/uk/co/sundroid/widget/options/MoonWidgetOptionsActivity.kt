package uk.co.sundroid.widget.options

import uk.co.sundroid.widget.config.MoonWidgetConfigActivity

class MoonWidgetOptionsActivity : AbstractWidgetOptionsActivity() {

    override val configClass: Class<*>
        get() = MoonWidgetConfigActivity::class.java
    override val name: String
        get() = "Moon Widget"

}