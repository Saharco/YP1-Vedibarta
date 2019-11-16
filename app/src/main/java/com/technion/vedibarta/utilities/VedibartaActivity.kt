package com.technion.vedibarta.utilities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.view.WindowManager
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.technion.vedibarta.POJOs.Student


/**
 * This is a utility activity with no GUI
 */
@SuppressLint("Registered")
open class VedibartaActivity : AppCompatActivity() {

    protected var student: Student? = null

    override fun onStart() {
        super.onStart()
        if (student == null) {
            //TODO: redirect to LoginActivity
        }
    }

    /**
     * Changes the status bar's color (only works on API 21+)
     *
     * @param color: the selected color for the status bar
     */
    protected fun changeStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    /**
     * Hides the virtual keyboard in the activity, if it is open
     *
     * @param activity: activity in which the keyboard should be hidden
     */
    protected fun hideKeyboard(activity: Activity) {
        val imm = activity
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            // There is no view to pass the focus to, so we create a new view
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}