package uk.co.sundroid.widget.options

import uk.co.sundroid.widget.config.SunMoonWidgetConfigActivity

class SunMoonWidgetOptionsActivity : AbstractWidgetOptionsActivity() {

    override val configClass: Class<*>
        get() = SunMoonWidgetConfigActivity::class.java
    override val name: String
        get() = "Sun & Moon Widget"

}