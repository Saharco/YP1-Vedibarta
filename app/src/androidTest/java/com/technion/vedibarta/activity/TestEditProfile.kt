package com.technion.vedibarta.activity


import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.facebook.appevents.internal.ActivityLifecycleTracker.getCurrentActivity
import com.technion.vedibarta.activity.Utillities.Companion.sleep
import com.technion.vedibarta.activity.Utillities.Companion.waitForActivity
import com.technion.vedibarta.activity.Utillities.Companion.TableLayoutHandler
import com.technion.vedibarta.R
import com.technion.vedibarta.activity.Utillities.Companion.partialyHiddenViewClick
import com.technion.vedibarta.login.LoginActivity
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.userProfile.ProfileEditActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestEditProfile
{
    private val registry = IdlingRegistry.getInstance()
    private val splashScreen = VedibartaActivity.splashScreen

    @get:Rule
    var loginActivity = ActivityTestRule(LoginActivity::class.java)


    @Before
    fun setUp()
    {
        registry.register(splashScreen)
        waitForActivity<MainActivity>()
        onView(withId(R.id.user_profile)).perform(click())
        waitForActivity<UserProfileActivity>()
    }

    @After
    fun breakDown()
    {
        registry.unregister(splashScreen)
    }

    @Test
    fun selecting_and_deselecting_characteristics()
    {
        onView(withId(R.id.actionEditProfile)).perform(click())

        val table = TableLayoutHandler()
        table.bindTable(R.id.bubblesRecycleView)
            .forTable(click())
            .scrollToRowAt(0)
            .forTable(click())
        sleep(1)
    }

    @Test
    fun selecting_and_deselecting_hobbies()
    {
        onView(withId(R.id.actionEditProfile)).perform(click())
        onView(withId(R.id.editProfileContainer)).perform(swipeLeft())
        sleep(1)

        val table = TableLayoutHandler()
        val recycler = (getCurrentActivity() as ProfileEditActivity).findViewById<RecyclerView>(R.id.hobbyTitlesList)
        val numCategories = recycler.adapter!!.itemCount

        for (i in 0 until numCategories)
        {
            onView(withId(R.id.hobbyTitlesList)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(i, table.bindTableInsideRecyclerView(R.id.hobbiesTables)))
            table.forTable(partialyHiddenViewClick(50)).forTable(partialyHiddenViewClick(50))
            sleep(0.5)
        }
        sleep(0.5)
    }

    @Test
    fun profile_is_saved_when_finishing_editing()
    {
        onView(withId(R.id.actionEditProfile)).perform(click())
        val table = TableLayoutHandler()
        table.bindTable(R.id.bubblesRecycleView)
            .performAtPositionInTable(0,0, click())
            .performAtPositionInTable(0,1, click())
        sleep(1)

        //go to hobbies
        onView(withId(R.id.editProfileContainer)).perform(swipeLeft())
        sleep(1)

        onView(withId(R.id.hobbyTitlesList)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, table.bindTableInsideRecyclerView(R.id.hobbiesTables)))
        table.forTable(click())
        sleep(1)

        onView(withId(R.id.actionEditProfile)).perform(click())

        waitForActivity<UserProfileActivity>()
        sleep(2)
        onView(withId(R.id.scrollViewLayout)).perform(swipeUp())
        onView(withId(R.id.scrollViewLayout)).perform(swipeUp())
        onView(withId(R.id.scrollViewLayout)).perform(swipeUp())
        onView(withId(R.id.scrollViewLayout)).perform(swipeUp())
        sleep(2)
    }
}