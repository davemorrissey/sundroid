package uk.co.sundroid

import android.widget.DatePicker
import androidx.appcompat.widget.AppCompatImageButton
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.prefs.Prefs
import java.util.*

abstract class AbstractTest {

    protected fun setDateWithDatePicker(y: Int, m: Int, d: Int) {
        onView(withId(R.id.dateButton))
                .perform(click())
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
                .perform(PickerActions.setDate(y, m, d))
        onView(withId(android.R.id.button1))
                .perform(click())
    }

    protected fun setLocationInPrefs(location: LocationDetails) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs.saveSelectedLocation(targetContext, location)
    }

    protected fun setLocationWithSearch(name: String) {
        onView(allOf(
                withClassName(Matchers.equalTo(AppCompatImageButton::class.java.name)),
                withParent(withId(R.id.toolbar))
        ))
                .perform(click())
        onView(withText("Change location"))
                .perform(click())
        onView(withId(R.id.locOptionSearch))
                .perform(click())
        onView(withId(R.id.searchField))
                .perform(typeText(name))
        onView(withId(R.id.searchSubmit))
                .perform(click())
        onView(withId(R.id.searchLocName))
                .perform(click())
    }

    protected fun verifyEvent(tag: String, time: String, az: String? = null) {
        onView(allOf(
                withId(R.id.evtTime),
                withParent(withTagKey(R.attr.eventIndex, equalTo(tag)))
        )).check(matches(withText(time)))
        az?.let {
            onView(allOf(
                    withId(R.id.evtAz),
                    withParent(withTagKey(R.attr.eventIndex, equalTo(tag)))
            )).check(matches(withText(az)))
        }
    }

    companion object {
        private fun location(name: String, coords: String, tzId: String): LocationDetails {
            val pos = LatitudeLongitude(coords)
            val tz = TimeZoneDetail(TimeZone.getTimeZone(tzId), "")
            val location = LocationDetails(pos)
            location.name = name
            location.timeZone = tz
            return location
        }
        val BRISBANE = location("Brisbane", "2728S 15302E", "Australia/Sydney")
        val EDINBURGH = location("Edinburgh", "5557N 00312W", "Europe/London")
    }

}