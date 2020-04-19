//package com.technion.vedibarta.activity
//
//import android.view.View
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.IdlingRegistry
//import androidx.test.espresso.action.ViewActions.*
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.rule.ActivityTestRule
//import com.facebook.appevents.internal.ActivityLifecycleTracker.getCurrentActivity
//import com.technion.vedibarta.activity.Utillities.Companion.sleep
//import com.technion.vedibarta.activity.Utillities.Companion.waitForActivity
//import com.technion.vedibarta.R
//import com.technion.vedibarta.chatSearch.ChatSearchActivity
//import com.technion.vedibarta.login.LoginActivity
//import com.technion.vedibarta.main.MainActivity
//import com.technion.vedibarta.utilities.VedibartaActivity
//import com.technion.vedibarta.activity.Utillities.Companion.TableLayoutHandler
//import com.technion.vedibarta.activity.Utillities.Companion.firstWithText
//import com.technion.vedibarta.activity.Utillities.Companion.partialyHiddenViewClick
//import com.technion.vedibarta.activity.Utillities.Companion.setChecked
//import com.technion.vedibarta.chatCandidates.ChatCandidatesActivity
//import com.technion.vedibarta.chatRoom.ChatRoomActivity
//import org.hamcrest.BaseMatcher
//import org.hamcrest.Description
//import org.hamcrest.Matcher
//import org.hamcrest.TypeSafeMatcher
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class TestChatCandidates
//{
//    private val registry = IdlingRegistry.getInstance()
//    private val splashScreen = VedibartaActivity.splashScreen
//
//    @get:Rule
//    var loginActivity = ActivityTestRule(LoginActivity::class.java)
//
//
//    @Before
//    fun setUp()
//    {
//        registry.register(splashScreen)
//        waitForActivity<MainActivity>()
//        onView(withId(R.id.extendedFloatingActionButton)).perform(click())
//        waitForActivity<ChatSearchActivity>()
//    }
//
//    @After
//    fun breakDown()
//    {
//        registry.unregister(splashScreen)
//    }
//
//    @Test
//    fun can_not_search_without_choosing_characterising()
//    {
//        onView(withId(R.id.searchUserContainer)).perform(swipeLeft())
//        onView(withId(R.id.actionChatSearch)).perform(click())
//        sleep(0.5)
//        onView(withText("OK")).perform(click())
//        sleep(0.5)
//        onView(withId(R.id.schoolFilterSwitch)).perform(setChecked(true))
//        sleep(1)
//        onView(withId(R.id.schoolListSpinner)).perform(replaceText("מקיף ליאו בק"))
//        onView(withId(R.id.actionChatSearch)).perform(click())
//        sleep(0.5)
//        onView(withText("OK")).perform(click())
//        sleep(1)
//    }
//
//    @Test
//    fun can_search_chat_partner_by_only_characteristics()
//    {
//        val table = TableLayoutHandler()
//        table.bindTable(R.id.searchCharacteristics)
//        for (i in 0 until 3)
//            table.performAtPositionInTable(i, i, click())
//        onView(withId(R.id.actionChatSearch)).perform(click())
//        sleep(2)
//    }
//
//    //might return no matches found
//    @Test
//    fun can_search_chat_partner_by_characteristics_and_school()
//    {
//        //chooses characteristics
//        val table = TableLayoutHandler()
//        table.bindTable(R.id.searchCharacteristics)
//        for (i in 0 until 3)
//            table.performAtPositionInTable(i, i, click())
//
//        //chooses school
//        onView(withId(R.id.searchUserContainer)).perform(swipeLeft())
//        onView(withId(R.id.schoolFilterSwitch)).perform(setChecked(true))
//        sleep(1)
//        onView(withId(R.id.schoolListSpinner)).perform(replaceText("מקיף עש יגאל אלון"))
//
//        //search
//        onView(withId(R.id.actionChatSearch)).perform(click())
//        sleep(2)
//    }
//
//    @Test
//    fun automatically_starts_a_chat_after_choosing_partner_and_it_appears_in_main()
//    {
//        val table = TableLayoutHandler()
//        table.bindTable(R.id.searchCharacteristics)
//        table.performAtPositionInTable(0,0, click())
//        onView(withId(R.id.actionChatSearch)).perform(click())
//        waitForActivity<ChatCandidatesActivity>()
//        sleep(3)
//        onView(firstWithText(R.string.chat_candidate_accept_button_m)).perform(partialyHiddenViewClick(50))
//        waitForActivity<ChatRoomActivity>()
//        val activity = (getCurrentActivity() as ChatRoomActivity)
//        val db = activity.database
//        val chatId = activity.chatId
//        onView(withContentDescription("Navigate up")).perform(click())
//        waitForActivity<MainActivity>()
//
//        // cleanup
//        var deleted = false
//        db.chats().chatId(chatId).build().delete().addOnSuccessListener { deleted = true }
//        while (!deleted) {}
//        sleep(3)
//    }
//}