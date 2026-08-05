package com.shahartal.myquietchannel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.TextView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialogInstrumentedTest {

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    private val targetContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun resetAppState() {
        targetContext
            .getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        targetContext.stopService(Intent(targetContext, VolumeCycleService::class.java))
        VolumeCycleService.isRunning = false
    }

    @After
    fun stopService() {
        targetContext.stopService(Intent(targetContext, VolumeCycleService::class.java))
        VolumeCycleService.isRunning = false
    }

    @Test
    fun launchShowsPrimaryControlsAndDefaultValues() {
        launchActivity()

        onView(withId(R.id.statusText))
            .check(matches(withText(targetContext.getString(R.string.title_name_disabled, ""))))
            .check(matches(isDisplayed()))
        onView(withId(R.id.toggleButton))
            .check(matches(withText(R.string.start)))
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextStationSpinner))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
        onView(withId(R.id.radioCheckbox))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isNotChecked()))
        onView(withId(R.id.textViewNextNewsStr))
            .perform(scrollTo())
            .check(matches(withText(MainActivity.NEXT_HOURS)))
        onView(withId(R.id.editTextDuration))
            .perform(scrollTo())
            .check(matches(withText("4")))
    }

    @Test
    fun radioOnlyModeDisablesScheduleFieldsUntilUnchecked() {
        launchActivity()

        onView(withId(R.id.radioCheckbox))
            .perform(scrollTo(), click())
            .check(matches(isChecked()))
        onView(withId(R.id.textViewNextNewsStr))
            .check(matches(not(isEnabled())))
        onView(withId(R.id.editTextDuration))
            .check(matches(not(isEnabled())))

        onView(withId(R.id.radioCheckbox))
            .perform(scrollTo(), click())
            .check(matches(isNotChecked()))
        onView(withId(R.id.textViewNextNewsStr))
            .check(matches(isEnabled()))
        onView(withId(R.id.editTextDuration))
            .check(matches(isEnabled()))
    }

    @Test
    fun scheduleFieldsNormalizeInvalidInputOnFocusLoss() {
        launchActivity()

        onView(withId(R.id.editTextDuration))
            .perform(scrollTo(), click(), replaceText("99"), closeSoftKeyboard())
        onView(withId(R.id.textViewNextNewsStr))
            .perform(scrollTo(), click())
        onView(withId(R.id.editTextDuration))
            .perform(scrollTo())
            .check(matches(withText(VolumeCycleService.max_news_duration.toString())))

        onView(withId(R.id.editTextDuration))
            .perform(scrollTo(), click(), replaceText("0"), closeSoftKeyboard())
        onView(withId(R.id.textViewNextNewsStr))
            .perform(scrollTo(), click())
        onView(withId(R.id.editTextDuration))
            .perform(scrollTo())
            .check(matches(withText("1")))

        onView(withId(R.id.textViewNextNewsStr))
            .perform(scrollTo(), clearText(), closeSoftKeyboard())
        onView(withId(R.id.editTextDuration))
            .perform(scrollTo(), click())
        onView(withId(R.id.textViewNextNewsStr))
            .perform(scrollTo(), click())
        onView(withId(R.id.textViewNextNewsStr))
            .perform(scrollTo())
            .check(matches(withText(MainActivity.NEXT_HOURS)))
    }

    @Test
    fun stationAndLocationSpinnersUpdateDisplayedSelectionAndLocationText() {
        launchActivity()

        onView(withId(R.id.editTextStationSpinner))
            .perform(scrollTo(), click())
        onData(allOf(instanceOf(String::class.java), `is`("FM102")))
            .perform(click())
        onView(withId(R.id.editTextStationSpinner))
            .check(matches(withSpinnerText("FM102")))

        onView(withId(R.id.editTextLocationSpinner))
            .perform(scrollTo(), click())
        onData(allOf(instanceOf(String::class.java), `is`("US-New York-NY")))
            .perform(click())
        onView(withId(R.id.editTextLocationSpinner))
            .check(matches(withSpinnerText("US-New York-NY")))
        onView(withId(R.id.editTextLocation))
            .check(matches(withText("US-New York-NY")))
    }

    @Test
    fun songPeekButtonOnlyAppearsForGlglz() {
        launchActivity()

        selectStation("FM102")
        onView(withId(R.id.peekSongsButton))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        selectStation("גלגלצ")
        onView(withId(R.id.peekSongsButton))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        activityRule.activity.runOnUiThread {
            activityRule.activity.findViewById<TextView>(R.id.textViewCurrentSong).text = "Current song"
            activityRule.activity.findViewById<TextView>(R.id.textViewNextSong).text = "Next song"
        }

        selectStation("FM102")
        onView(withId(R.id.peekSongsButton))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.textViewCurrentSong))
            .check(matches(withText("")))
        onView(withId(R.id.textViewNextSong))
            .check(matches(withText("")))
    }

    @Test
    fun startButtonShowsVolumeSetupDialogAndUpdatesServiceControls() {
        launchActivity()

        selectStation("FM102")

        onView(withId(R.id.toggleButton))
            .perform(click())

        onView(withId(android.R.id.message))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button2))
            .inRoot(isDialog())
            .perform(click())

        onView(withId(R.id.toggleButton))
            .perform(scrollTo())
            .check(matches(withText(R.string.stop)))
        onView(withId(R.id.radioCheckbox))
            .perform(scrollTo())
            .check(matches(not(isEnabled())))

        onView(withId(R.id.toggleButton))
            .perform(scrollTo(), click())
        onView(withId(R.id.toggleButton))
            .check(matches(withText(R.string.start)))
    }

    private fun launchActivity() {
        activityRule.launchActivity(Intent())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(300)
    }

    private fun selectStation(station: String) {
        onView(withId(R.id.editTextStationSpinner))
            .perform(scrollTo(), click())
        onData(allOf(instanceOf(String::class.java), `is`(station)))
            .perform(click())
    }
}
