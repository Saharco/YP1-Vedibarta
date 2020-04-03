package com.technion.vedibarta.activity

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.facebook.appevents.internal.ActivityLifecycleTracker.getCurrentActivity
import com.technion.vedibarta.activity.Utillities.Companion.clickOnItemAtPosition
import com.technion.vedibarta.activity.Utillities.Companion.sleep
import com.technion.vedibarta.activity.Utillities.Companion.waitForActivity
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
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
class TestChatRoom
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
        onView(withId(R.id.chat_history)).perform(RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(0, click()))
        waitForActivity<ChatRoomActivity>()
    }

    @After
    fun breakDown()
    {
        //logout()
        registry.unregister(splashScreen)
    }

    @Test
    fun chat_automatically_scrolls_down_when_user_at_the_bottom_of_chat_and_sending_message()
    {
        onView(withId(R.id.chatBox)).perform(typeText("hi"))
        onView(withId(R.id.chatView)).perform(RecyclerViewActions.scrollToPosition< RecyclerView.ViewHolder>(0))
        sleep(1)
        onView(withId(R.id.buttonChatBoxSend)).perform(click())
        sleep(1)
        send("how are you?")
        send("what have you been up to since we have last seen each other?")
    }

    @Test
    fun chat_stays_still_when_sending_messages_when_user_isnt_at_the_bottom_of_chat()
    {
        val numMessages = (getCurrentActivity() as ChatRoomActivity).getNumMessages()
        //espresso can't initiate a real scroll (only teleports) so need to open keyboard manually before scrolling
        onView(withId(R.id.chatBox)).perform(click())
        sleep(0.3)
        onView(withId(R.id.chatView)).perform(RecyclerViewActions.scrollToPosition< RecyclerView.ViewHolder>(numMessages/2))
        sleep(0.5)
        send("hi")
        send("I really want some ice-cream now")
        send("ben & jeries would be nice right about now")
    }

    @Test
    fun system_message_is_sent_with_correct_question_when_using_the_question_generator()
    {
        onView(withId(R.id.popupMenu)).perform(click())
        onView(withText(R.string.generateQuestion)).perform(click())
        clickOnItemAtPosition<RecyclerView.ViewHolder>(R.id.questionCategoriesList, 0)
        sleep(0.5)
        clickOnItemAtPosition<RecyclerView.ViewHolder>(R.id.questionList, 1)
        sleep(2)
    }

    private fun send(s: String)
    {
        onView(withId(R.id.chatBox)).perform(typeText(s))
        onView(withId(R.id.buttonChatBoxSend)).perform(click())
        sleep(0.3)
    }
}