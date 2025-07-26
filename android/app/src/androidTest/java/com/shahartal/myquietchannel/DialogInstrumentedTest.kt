package com.shahartal.myquietchannel

//import org.junit.Assert.*

import android.Manifest
import android.content.Intent
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.assertion.ViewAssertions
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.RootMatchers
//import androidx.test.espresso.matcher.RootMatchers.isDialog
//import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
//import androidx.test.espresso.matcher.ViewMatchers
//import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
//import androidx.test.espresso.matcher.ViewMatchers.withId
//import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
//import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// patience: this can take 15..90 seconds to run

@RunWith(AndroidJUnit4::class)
class DialogInstrumentedTest {

    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule? =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun intentKicksOff() {

        val intent = Intent()
        // Customize intent if needed (maybe some extras?)
        activityRule.launchActivity(intent)

//        onView(withText("Is the radio app playing? Maybe the volume is at zero?!"))
//            .perform(click())

    //        onView(withText("Is the radio app playing? Maybe the volume is at zero?!"))
//            .inRoot(isDialog())
//            .perform(ViewActions.pressBack());

//        onView(withText(R.string.alert_title))
    //        .perform(ViewActions.pressBack())

//        onView(withText(containsString("A4")))
    //        .inRoot(isPlatformPopup())
    //        .check(matches(isDisplayed()));

//        onView(withId(R.id.toggleButton))
//
//            .inRoot(isPlatformPopup()) // not(RootMatchers.isDialog()))
//
//            .perform(click())
//            .check(matches(isDisplayed()))
//
//        onView(ViewMatchers.withText(R.string.alert_title))
//            .inRoot(RootMatchers.isDialog())
//            .inRoot(isPlatformPopup())
//            .check(ViewAssertions.matches(isDisplayed()))
    }


}