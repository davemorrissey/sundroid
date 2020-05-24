package uk.co.sundroid.widget.options

import uk.co.sundroid.widget.config.SunWidgetConfigActivity

class SunWidgetOptionsActivity : AbstractWidgetOptionsActivity() {

    override val configClass: Class<*>
        get() = SunWidgetConfigActivity::class.java
    override val name: String
        get() = "Sun Widget"

}