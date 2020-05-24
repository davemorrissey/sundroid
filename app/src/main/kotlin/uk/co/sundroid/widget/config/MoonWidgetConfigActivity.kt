package uk.co.sundroid.widget.config

import android.os.Bundle
import android.view.View
import uk.co.sundroid.util.prefs.Prefs

class MoonWidgetConfigActivity : AbstractWidgetConfigurationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b.header.text = "Moon Widget"
        b.intro.text = "This widget displays the moonrise and moonset times for your location, updated every 6 hours. You can select a saved location to save battery - just open the app, find and save the location you want, then select it below."
        b.boxOpacity.visibility = View.VISIBLE
        b.boxOpacitySeek.progress = Prefs.widgetBoxShadowOpacity(this, rnc.widgetId)
        b.boxOpacitySeek.setOnSeekBarChangeListener(this)
        setContentView(b.root)
    }

}