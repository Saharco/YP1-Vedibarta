package com.technion.vedibarta.activity

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.technion.vedibarta.R
import com.technion.vedibarta.login.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.technion.vedibarta.activity.Utillities.Companion.logout
import com.technion.vedibarta.activity.Utillities.Companion.sleep
import com.technion.vedibarta.utilities.VedibartaActivity
import org.junit.After
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class TestLogIn
{
    private val registry = IdlingRegistry.getInstance()
    private val splashScreen = VedibartaActivity.splashScreen

    @get:Rule
    var loginActivity = ActivityTestRule(LoginActivity::class.java)


    @Before
    fun setUp()
    {
        registry.register(splashScreen)
    }

    @After
    fun breakDown()
    {
        registry.unregister(splashScreen)
    }

    @Test
    fun login_with_mail_and_then_logout()
    {

        val email = "notmyactualacount1@gmail.com"
        val password = "123456"
        onView(withId(R.id.sign_in_link)).perform(click())
        onView(withId(R.id.email_input_edit_text)).perform(typeText(email))
        onView(withId(R.id.password_input_edit_text)).perform(typeText(password))
        onView(withId(R.id.login_button)).perform(click())
        sleep(2)
        logout()
    }

    @Test
    fun login_with_google_and_then_exit()
    {
        onView(withId(R.id.google_login_button)).perform(click())
        sleep(5)
        logout()
    }


    private fun login_with_facebook_and_then_exit()
    {
        //espresso stops running once opening the facebook GUI, no way around it
    }

    @Test
    fun fail_to_login_with_mail_and_then_succeed_and_then_exit()
    {
        //failure
        onView(withId(R.id.sign_in_link)).perform(click())
        onView(withId(R.id.email_input_edit_text)).perform(typeText("doesNot@Exists.haha"))
        onView(withId(R.id.password_input_edit_text)).perform(typeText(":D"))
        onView(withId(R.id.login_button)).perform(click())
        sleep(2)

        //clear
        onView(withId(R.id.email_input_edit_text)).perform(clearText())
        onView(withId(R.id.password_input_edit_text)).perform(clearText())

        //success
        val email = "notmyactualacount1@gmail.com"
        val password = "123456"
        onView(withId(R.id.email_input_edit_text)).perform(typeText(email))
        onView(withId(R.id.password_input_edit_text)).perform(typeText(password))
        onView(withId(R.id.login_button)).perform(click())
        sleep(2)
        logout()
    }
}