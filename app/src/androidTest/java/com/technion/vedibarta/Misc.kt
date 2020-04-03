package com.technion.vedibarta

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.technion.vedibarta.activity.Utillities
import com.technion.vedibarta.activity.Utillities.Companion.waitForActivity
import com.technion.vedibarta.login.LoginActivity
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Misc
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
    fun logout()
    {
        waitForActivity<MainActivity>()
        Utillities.logout()
    }

    @Test
    fun logInWithMail()
    {
        val email = "notmyactualacount1@gmail.com"
        val password = "123456"
        Utillities.loginWithMail(email, password)
    }

    @Test
    fun logInForSetUpTest()
    {
        val email = "ron.gr@campus.technion.ac.il"
        val password = "12345678"
        Utillities.loginWithMail(email, password)
    }
}