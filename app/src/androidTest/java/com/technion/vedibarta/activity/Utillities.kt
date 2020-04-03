package com.technion.vedibarta.activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import java.lang.Thread
import android.content.res.Resources
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.contrib.RecyclerViewActions
import com.facebook.appevents.internal.ActivityLifecycleTracker.getCurrentActivity
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.view.children
import androidx.test.espresso.ViewAction
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import kotlin.math.min
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.espresso.UiController
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.allOf
import android.widget.Checkable
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.Tapper
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.core.internal.deps.guava.base.Optional
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import androidx.test.orchestrator.junit.BundleJUnitUtils.getDescription
import com.technion.vedibarta.R
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.isA
import java.lang.Exception


class Utillities
{
    /***
     * requires to be on the MainActivity screen before call
     */
    companion object
    {
        fun printViewHierarchy(vg: ViewGroup, prefix: String) {
            for (i in 0 until vg.childCount) {
                val v = vg.getChildAt(i)
                val desc =
                    prefix + " | " + "id=${v.id} | " + "[" + i + "/" + (vg.childCount - 1) + "] " + v.javaClass.simpleName + " " + v.id
                Log.d("wtf", desc)

                if (v is ViewGroup) {
                    printViewHierarchy(v, desc)
                }
            }
        }

        fun logout()
        {
            onView(withId(com.technion.vedibarta.R.id.action_user_profile)).perform(click())
            onView(withId(com.technion.vedibarta.R.id.actionLogOut)).perform(click())
            onView(withText("כן")).perform(click()) //TODO replace the hardcoded string
            sleep(0.5)
        }

        fun loginWithMail(mail: String, pass: String)
        {
            onView(withId(com.technion.vedibarta.R.id.sign_in_link)).perform(click())
            onView(withId(com.technion.vedibarta.R.id.email_input_edit_text))
                .perform(ViewActions.replaceText(mail))
            onView(withId(com.technion.vedibarta.R.id.password_input_edit_text))
                .perform(ViewActions.replaceText(pass))
            sleep(1)
            onView(withId(com.technion.vedibarta.R.id.login_button)).perform(click())
        }


        fun sleep(seconds: Int)
        {
            Thread.sleep((seconds*1000).toLong())
        }
        fun sleep(seconds: Double)
        {
            Thread.sleep((seconds*1000).toLong())
        }

        inline fun <reified  T: AppCompatActivity> waitForActivity()
        {
            var activityT = getCurrentActivity() as? T
            while (activityT == null || !activityT.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            {
                sleep(0.1)
                activityT = getCurrentActivity() as? T
            }
            sleep(1) //just in case to offset adapter loading time
        }

        inline fun <reified T: RecyclerView.ViewHolder> clickOnItemAtPosition
                    (recyclerViewId: Int,
                     postion: Int)
        {
            onView(withId(recyclerViewId)).perform(RecyclerViewActions.actionOnItemAtPosition<T>(postion, click()))
        }

        fun isVisible(view: View?): Boolean
        {
            if (view == null) {
                return false
            }
            if (!view.isShown) {
                return false
            }
            val actualPosition = Rect()
            view.getGlobalVisibleRect(actualPosition)
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val screen = Rect(0, 0, screenWidth, screenHeight)
            Log.d("wtf", "$actualPosition, $screen")
            return screen.contains(actualPosition)
        }

        fun setChecked(checked: Boolean): ViewAction
        {
            return object : ViewAction {

                override fun getConstraints(): BaseMatcher<View>
                {
                    return object : BaseMatcher<View>()
                    {
                        override fun matches(item: Any): Boolean {

                            return isA(Checkable::class.java).matches(item)
                        }

                        override fun describeMismatch(item: Any, mismatchDescription: Description) {}

                        override fun describeTo(description: Description) {}
                    }
                }

                override fun getDescription(): String? {
                    return null
                }

                override fun perform(uiController: UiController, view: View) {
                    val checkableView = view as Checkable
                    checkableView.isChecked = checked
                }
            }
        }

        fun partialyHiddenViewClick(areaPercentage: Int): ViewAction
        {
            return object: ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isDisplayingAtLeast(areaPercentage)
                }

                override fun getDescription(): String {
                    return "click plus button"
                }

                override fun perform(uiController: UiController, view: View) {
                    view.performClick()
                    sleep(0.2)
                }
            }
        }

        fun firstWithText(resourceId: Int): Matcher<View>
        {
            return object: BaseMatcher<View>(){
                private var found = false
                private val innerMatcher = withText(resourceId)
                override fun describeTo(description: Description?) {
                    description?.appendText("is first matching view with resourse $resourceId")
                }

                override fun matches(item: Any?): Boolean
                {
                    if (innerMatcher.matches(item) && !found)
                    {
                        found = true
                        return true
                    }
                    return false
                }

            }
        }

        class TableLayoutHandler
        {
            private var numRows = 0
            private var rowSize = 0
            private var lastRowSize = 0
            private var lastViewableRowIndex = -1
            private lateinit var table: TableLayout
            private lateinit var rows: Sequence<View>

            fun bindTable(id:Int): TableLayoutHandler
            {
                onView(allOf(withTable(id))).check(matches(isDisplayed()))
                return this
            }

            fun bindTableInsideRecyclerView(id: Int): ViewAction
            {
                return object: ViewAction
                {
                    override fun getDescription(): String {
                        return "binding table inside RecyclerView"
                    }

                    override fun getConstraints(): Matcher<View>
                    {
                        return allOf(isDisplayed(), isAssignableFrom(View::class.java))
                    }

                    override fun perform(uiController: UiController?, view: View?)
                    {
                        view?.let {
                            val table = view.findViewById<TableLayout>(id)
                            extractTableProperties(table)
                        }
                    }

                }
            }

            private fun atPositionInTable(rowPos: Int, colPos: Int): Matcher<View>
            {
                Log.d("wtf", "$rowPos, $colPos")
                return object : TypeSafeMatcher<View>()
                {
                    private var found = false

                    override fun describeTo(description: Description)
                    {
                        description.appendText("is at position # row=$rowPos , col=$colPos")
                    }

                    override fun matchesSafely(view: View): Boolean
                    {
                        val tableRow = view.parent
                        val tableLayout = tableRow.parent
                        if (tableRow !is TableRow || tableLayout !is TableLayout || tableLayout != table)
                            return false

                        val row = table.indexOfChild(tableRow)
                        val col = tableRow.indexOfChild(view)
                        if (row == rowPos && col == colPos && !found)
                        {
                            found = true
                            return true
                        }
                        return false
                    }
                }
            }

            fun performAtPositionInTable(rowPos: Int, colPos: Int, action: ViewAction): TableLayoutHandler
            {
                scrollToRowAt(rowPos)
                onView(atPositionInTable(rowPos, colPos)).perform(action)
                return this
            }

            fun forTable(action: ViewAction): TableLayoutHandler
            {
                Log.d("wtf", "============")
                scrollToRowAt(0)
                var lastViewableRow = lastViewableRowIndex
                for (row in 0 until numRows-1) //last iteration is second to last row
                    for (col in 0 until rowSize)
                    {
                        if (row == lastViewableRow)
                        {
                            lastViewableRow++
                            lastViewableRow = min(lastViewableRow, numRows-1)
                            onView(atPositionInTable(lastViewableRow, 0)).perform(ViewActions.scrollTo())
                        }
                        onView(atPositionInTable(row,col)).perform(action)
                    }
                for (col in 0 until lastRowSize)
                    onView(atPositionInTable(numRows-1,col)).perform(action)

                return this
            }

            fun scrollToRowAt(index: Int): TableLayoutHandler
            {
                if (!isVisible(rows.elementAt(index)))
                    onView(atPositionInTable(index, 0)).perform(ViewActions.scrollTo())
                return this
            }

            private fun withTable(id:Int): Matcher<View>
            {
                return object : TypeSafeMatcher<View>() {
                    override fun describeTo(description: Description)
                    {
                        description.appendText("extracting tableLayout properties")
                    }

                    override fun matchesSafely(view: View): Boolean
                    {
                        if (view !is TableLayout || view.id != id)
                            return false

                        extractTableProperties(view)
                        return true
                    }
                }
            }

            private fun extractTableProperties(view: TableLayout)
            {
                table = view

                numRows = table.childCount
                if (numRows == 0)
                    return

                rows = table.children
                val firstRow = rows.first() as TableRow
                val lastRow = rows.last() as TableRow
                rowSize = firstRow.childCount
                lastRowSize = lastRow.childCount

                lastViewableRowIndex = -1
                for (row in rows)
                {
                    if (!isVisible(row))
                        break
                    lastViewableRowIndex++
                }
            }
        }
    }
}