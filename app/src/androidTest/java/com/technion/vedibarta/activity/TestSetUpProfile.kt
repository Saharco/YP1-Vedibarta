package com.technion.vedibarta.activity

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.facebook.appevents.internal.ActivityLifecycleTracker.getCurrentActivity
import com.technion.vedibarta.activity.Utillities.Companion.sleep
import com.technion.vedibarta.activity.Utillities.Companion.waitForActivity
import com.technion.vedibarta.R
import com.technion.vedibarta.activity.Utillities.Companion.logout
import com.technion.vedibarta.activity.Utillities.Companion.partialyHiddenViewClick
import com.technion.vedibarta.login.LoginActivity
import com.technion.vedibarta.login.UserSetupActivity
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestSetUpProfile
{
    private val registry = IdlingRegistry.getInstance()
    private val splashScreen = VedibartaActivity.splashScreen

    @get:Rule
    var loginActivity = ActivityTestRule(LoginActivity::class.java)

    @Before
    fun setUp()
    {
        registry.register(splashScreen)
        val email = "ron.gr@campus.technion.ac.il"
        val password = "12345678"
        Utillities.loginWithMail(email, password)
        waitForActivity<UserSetupActivity>()
        VedibartaActivity.hideKeyboard(getCurrentActivity() as UserSetupActivity)
    }

//    @After
//    fun breakDown()
//    {
//        onView(ViewMatchers.withContentDescription("Navigate up")).perform(click())
//        waitForActivity<MainActivity>()
//        val db = (getCurrentActivity() as MainActivity).database
//        var deleted = false
//        db.students().userId().build()
//            .delete()
//            .addOnSuccessListener { deleted = true }
//        while (!deleted) {}
//        logout()
//        waitForActivity<LoginActivity>()
//        registry.unregister(splashScreen)
//    }

    @Test
    fun can_finish_setUp_only_after_filling_all_fields()
    {
        //try to finish on choose_gender
        failedFinishClick()
        sleep(0.3)
        onView(withId(R.id.imageFemale)).perform(partialyHiddenViewClick(20))
        failedFinishClick()

        //try to finish on choose_extra_options
        onView(withId(R.id.textFieldFirstName)).perform(replaceText("יוצרת"))
        sleep(0.3)
        failedFinishClick()


        onView(withId(R.id.textFieldLastName)).perform(scrollTo()).perform(replaceText("משתמש"))
        sleep(0.3)
        failedFinishClick()

        onView(withId(R.id.schoolListSpinner)).perform(scrollTo()).perform(replaceText("מקיף ליאו בק"))
        onView(withId(R.id.regionListSpinner)).perform(scrollTo()).perform(replaceText("חיפה"))
        sleep(0.3)
        failedFinishClick()

        //next screen
        onView(withId(R.id.userSetupContainer)).perform(swipeLeft())

        //try to finish on choose_characteristics
        val table = Utillities.Companion.TableLayoutHandler()
        sleep(0.5)
        table.bindTable(R.id.searchCharacteristics).performAtPositionInTable(0,0, click())
        failedFinishClick()

        //next screen
        onView(withId(R.id.userSetupContainer)).perform(swipeLeft())

        //try to finish on choose_hobbies
        sleep(0.5)
        onView(withId(R.id.hobbyTitlesList)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, table.bindTableInsideRecyclerView(R.id.hobbiesTables)))
        table.performAtPositionInTable(0,0, click())
        sleep(0.3)

        //success
        onView(withId(R.id.doneButton)).perform(click())
        waitForActivity<UserProfileActivity>()
    }

    private fun failedFinishClick()
    {
        onView(withId(R.id.doneButton)).perform(click())
        sleep(0.5)
        onView(withText(R.string.ok)).perform(click())
        sleep(0.5)
    }
}