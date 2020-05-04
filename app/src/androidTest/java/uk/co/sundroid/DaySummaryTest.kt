package uk.co.sundroid


import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.util.prefs.ClockType
import uk.co.sundroid.util.prefs.Prefs


@RunWith(AndroidJUnit4::class)
@LargeTest
class DaySummaryTest : AbstractDayFragmentTest() {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Before
    fun init() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs.setLastDataGroup(targetContext, DataGroup.DAY_SUMMARY)

        // Avoid new install and upgrade dialogs
        Prefs.setVersion(targetContext, Int.MAX_VALUE)

        // Reset to standard preferences
        Prefs.setShowSeconds(targetContext, true)
        Prefs.setShowTimeZone(targetContext, true)
        Prefs.setClockType(targetContext, ClockType.TWENTYFOUR)

        // Set to Brisbane
        setLocationInPrefs(BRISBANE)
    }

    @Test
    fun testExpectedDataForEdinburgh() {
        setLocationWithSearch("Edinburgh")
        setDateWithDatePicker(2020, 1, 10)
        verifyEvent("sunEvt0", "08:40", "130.7\u00b0")
        verifyEvent("sunEvt1", "16:00", "229.4\u00b0")
    }

    @Test
    fun testExpectedDataForTromso() {
        setLocationWithSearch("Tromso")
        setDateWithDatePicker(2020, 1, 10)
        verifyEvent("sunSpecial", "SET ALL DAY")
        setDateWithDatePicker(2020, 4, 16)
        verifyEvent("sunEvt0", "04:36", "56.6\u00b0")
        verifyEvent("sunEvt1", "20:55", "304.2\u00b0")
        setDateWithDatePicker(2020, 6, 10)
        verifyEvent("sunSpecial", "RISEN ALL DAY")
    }

    @Test
    fun testExpectedDataForBrisbane() {
        setLocationWithSearch("Brisbane")
        setDateWithDatePicker(2020, 1, 10)
        verifyEvent("sunEvt0", "05:01", "115.7\u00b0")
        verifyEvent("sunEvt1", "18:48", "244.4\u00b0")
    }

}
