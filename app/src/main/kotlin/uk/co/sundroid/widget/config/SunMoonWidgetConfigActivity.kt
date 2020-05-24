package uk.co.sundroid.widget.config

import android.os.Bundle
import android.view.View
import uk.co.sundroid.util.prefs.Prefs

class SunMoonWidgetConfigActivity : AbstractWidgetConfigurationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b.header.text = "Sun & Moon Widget"
        b.introWarning.visibility = View.VISIBLE
        b.introWarning.text = "This widget will fit in a 2x1 space on some phones, but requires a 2x2 space on others. Please resize it as necessary."
        b.intro.text = "This widget displays sun and moon rise and set times for your location, updated every 6 hours. You can select a saved location to save battery - just open the app, find and save the location you want, then select it below."
        b.boxOpacity.visibility = View.VISIBLE
        b.boxOpacitySeek.progress = Prefs.widgetBoxShadowOpacity(this, rnc.widgetId)
        b.boxOpacitySeek.setOnSeekBarChangeListener(this)
        setContentView(b.root)
    }

}