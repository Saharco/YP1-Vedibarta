package com.technion.vedibarta.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.technion.vedibarta.POJOs.Student
import java.sql.Timestamp

/**
 * This is a utility activity with no GUI
 */
@SuppressLint("Registered")
open class VedibartaActivity : AppCompatActivity()
{
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val storage = Storage(userId)
    val database = DocumentsCollections(userId)
    //TODO: for now this will configure a default student. change this to null later
    companion object {
        var student: Student? = null

        /**
         * @param resources: resources object of the current context
         *
         * @return [dp] as pixels on the current screen
         */
        fun dpToPx(resources: Resources, dp: Float): Float =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

        /**
         * Hides the virtual keyboard in the activity, if it is open
         *
         * @param activity: activity in which the keyboard should be hidden
         */
        fun hideKeyboard(activity: Activity) {
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

    protected val IMAGE_COMPRESSION_QUALITY_IN_PERCENTS: Int = 90

    override fun onStart() {
        super.onStart()
        if (student == null) {
            //TODO: redirect to LoginActivity here!!!
        }
    }

    /**
     * @return [this] as pixels on the current screen
     */
    fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

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



}