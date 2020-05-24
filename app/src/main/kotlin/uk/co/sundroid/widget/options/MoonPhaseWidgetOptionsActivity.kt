package uk.co.sundroid.widget.options

import uk.co.sundroid.widget.config.MoonPhaseWidgetConfigActivity

class MoonPhaseWidgetOptionsActivity : AbstractWidgetOptionsActivity() {

    override val configClass: Class<*>
        get() = MoonPhaseWidgetConfigActivity::class.java
    override val name: String
        get() = "Moon Phase Widget"

}