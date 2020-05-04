package uk.co.sundroid


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
abstract class AbstractDayFragmentTest : AbstractTest() {

    @Test
    fun testDateNavigation() {
        setDateWithDatePicker(2020, 6, 19)

        onView(withId(R.id.dateWeekday))
                .check(matches(withText("Friday")))
        onView(withId(R.id.dateDMY))
                .check(matches(withText("19 Jun 2020")))

        onView(withId(R.id.datePrev))
                .perform(click())

        onView(withId(R.id.dateWeekday))
                .check(matches(withText("Thursday")))
        onView(withId(R.id.dateDMY))
                .check(matches(withText("18 Jun 2020")))

        onView(withId(R.id.dateNext))
                .perform(click())
                .perform(click())

        onView(withId(R.id.dateWeekday))
                .check(matches(withText("Saturday")))
        onView(withId(R.id.dateDMY))
                .check(matches(withText("20 Jun 2020")))

        onView(withId(R.id.dateButton))
                .perform(swipeUp())
        onView(withId(R.id.dateWeekday))
                .check(matches(withText("Wednesday")))
        onView(withId(R.id.dateDMY))
                .check(matches(withText("20 May 2020")))

        onView(withId(R.id.dateButton))
                .perform(swipeLeft())
        onView(withId(R.id.dateWeekday))
                .check(matches(withText("Tuesday")))
        onView(withId(R.id.dateDMY))
                .check(matches(withText("19 May 2020")))

        onView(withId(R.id.dateButton))
                .perform(swipeDown())
        onView(withId(R.id.dateWeekday))
                .check(matches(withText("Friday")))
        onView(withId(R.id.dateDMY))
                .check(matches(withText("19 Jun 2020")))

        onView(withId(R.id.dateButton))
                .perform(swipeRight())
        onView(withId(R.id.dateWeekday))
                .check(matches(withText("Saturday")))
        onView(withId(R.id.dateDMY))
                .check(matches(withText("20 Jun 2020")))
    }
}
