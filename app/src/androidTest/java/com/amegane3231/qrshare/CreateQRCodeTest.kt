package com.amegane3231.qrshare

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import com.amegane3231.qrshare.ui.activities.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class CreateQRCodeTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun checkBeforeUploadTest() {
        val fabAddQRCode = onView(withId(R.id.fab_add_QR_code))
        fabAddQRCode.perform(click())

        val fabCreateQRCode = onView(withId(R.id.fab_create_QR_code))
        fabCreateQRCode.perform(click())

        val urlEditText = onView(withId(R.id.edittext_input_URL))
        urlEditText.perform(typeText("https://amegane.com/aaabbb"))
        urlEditText.perform(pressBack())

        val tagEditText = onView(withId(R.id.edittext_input_tag))
        tagEditText.perform(typeText("#Test"))

        val submit = onView(withId(R.id.action_submit))
        submit.check(matches(isClickable()))
    }
}