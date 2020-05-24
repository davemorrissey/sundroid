package uk.co.sundroid.widget.config

import android.os.Bundle
import android.view.View
import uk.co.sundroid.util.prefs.Prefs

class MoonPhaseWidgetConfigActivity : AbstractWidgetConfigurationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b.header.text = "Moon Phase Widget"
        b.intro.text = "This widget shows an approximate view of the moon as it appears at your location, updated every hour. Lunar librations are not simulated.\n\nYou can scale the widget by long pressing it and dragging the handles."
        b.shadowOpacity.visibility = View.VISIBLE
        b.shadowSize.visibility = View.VISIBLE
        b.shadowOpacitySeek.progress = Prefs.widgetPhaseShadowOpacity(this, rnc.widgetId)
        b.shadowSizeSeek.progress = Prefs.widgetPhaseShadowSize(this, rnc.widgetId)
        b.shadowOpacitySeek.setOnSeekBarChangeListener(this)
        b.shadowSizeSeek.setOnSeekBarChangeListener(this)
        setContentView(b.root)
    }

}