package com.technion.vedibarta.activity

import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.technion.vedibarta.activity.Utillities.Companion.sleep
import com.technion.vedibarta.activity.Utillities.Companion.waitForActivity
import com.technion.vedibarta.R
import com.technion.vedibarta.login.LoginActivity
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.main.ViewHolder
import com.technion.vedibarta.utilities.VedibartaActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestMain
{
    private val registry = IdlingRegistry.getInstance()
    private val splashScreen = VedibartaActivity.splashScreen

    @get:Rule
    var loginActivity = ActivityTestRule(LoginActivity::class.java)


    @Before
    fun setUp()
    {
        registry.register(splashScreen)
        //loginWithMail()
        waitForActivity<MainActivity>()
    }

    @After
    fun breakDown()
    {
        //logout()
        registry.unregister(splashScreen)
    }

    //the test uses an english name because espresso can't type text in a different language then the device preference
    @Test
    fun search_for_specific_chat_name()
    {
        onView(withId(R.id.search)).perform(click())
        onView(isAssignableFrom(EditText::class.java)).perform(typeText("ron"))
        sleep(1)
        onView(withId(com.miguelcatalan.materialsearchview.R.id.action_up_btn)).perform(click())
        sleep(0.2)
    }

    @Test
    fun enter_chat_room_from_main_activity_and_then_go_back()
    {
        onView(withId(R.id.chat_history)).perform(RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(0, click()))
        sleep(0.3)
        onView(withContentDescription("Navigate up")).perform(click())
        sleep(0.2)
    }

    @Test
    fun chat_moves_to_top_of_history_after_sending_a_message()
    {
        onView(withId(R.id.chat_history)).perform(RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(2, click()))
        sleep(1)
        onView(withId(R.id.chatBox)).perform(typeText("howdy neighbor"))
        onView(withId(R.id.buttonChatBoxSend)).perform(click())
        sleep(1.5)
        onView(withContentDescription("Navigate up")).perform(click())
        sleep(3)
    }
}