package com.technion.vedibarta.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.skyfishjy.library.RippleBackground
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.dagger.DataBaseInjector
import com.technion.vedibarta.login.LoginActivity
import javax.inject.Inject

/**
 * This is a utility activity with no GUI
 */
@SuppressLint("Registered")
open class VedibartaActivity : AppCompatActivity()
{
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val storage = Storage(userId)
    @Inject
    lateinit var database: DataBase

    init
    {
        // has to be here, otherwise it would need to be called from every activity which extends Vedibarta
        DataBaseInjector.injector.inject(this)
    }

    private var progress: ProgressDialog? = null
    private var progressHandler: Handler? = null
    protected var loadTimeout: Int = 5000

    // A task that's executed when a time-out occurs with a loading process
    protected val loadTimeoutTask = Runnable {

        if (progress != null)
        {
            progress!!.dismiss()
        }

        val title = TextView(this)
        title.setText(R.string.dialog_timeout_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(resources.getColor(R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)

        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
                .setIcon(R.drawable.ic_warning_yellow)
                .setMessage(R.string.dialog_timeout_body)
                .setNeutralButton(android.R.string.ok) { _, _ -> onBackPressed() }
        val dialog = builder.create()
        dialog.show()

        // Display "OK" button in the middle
        val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        val buttonLayoutParams =
            neutralButton.layoutParams as LinearLayout.LayoutParams
        buttonLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        neutralButton.layoutParams = buttonLayoutParams
    }

    companion object
    {
        var student: Student? = null
        var chatPartnerId: String? = null
        var isActivityRunning = false

        const val IMAGE_COMPRESSION_QUALITY_IN_PERCENTS = 90
        const val EXTRA_CHANGE_ACTIVITY = "EXTRA_CHANGE_ACTIVITY"

        //needed for espresso tests
        val splashScreen = CountingIdlingResource("splashScreen")


        /**
         * @param resources: resources object of the current context
         *
         * @return [dp] as pixels on the current screen
         */
        fun dpToPx(resources: Resources, dp: Float): Float =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

        /**
         * Hides the virtual keyboard in the activity, if it is open
         *`
         * @param activity: activity in which the keyboard should be hidden
         */
        fun hideKeyboard(activity: Activity)
        {
            val imm = activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null)
            {
                // There is no view to pass the focus to, so we create a new view
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * Changes the activity's layout to the splash screen's.
         * The splash's screen must be *included* into the activity's layout.
         * This should be called when there's a background task that starts a new activity when finished.
         *
         * @param activity: current context
         * @param loadMessage: the message to be displayed on the splash screen
         */
        fun showSplash(activity: Activity, loadMessage: String)
        {
            splashScreen.increment()
            try
            {
                val rippleBackground =
                    activity.findViewById<RippleBackground>(R.id.rippleBackground)
                val splashText = activity.findViewById<TextView>(R.id.splashText)
                changeStatusBarColor(activity,
                                     ContextCompat.getColor(activity, R.color.backgroundSplash))
                splashText.text = loadMessage
                rippleBackground.startRippleAnimation()

            }
            catch (e: Exception)
            {
                splashScreen.decrement() //just in case of failure
            }
        }

        /**
         * Cancels the splash's animation and returns the status bar color back to its default color
         *
         * @param activity: current context
         */
        fun hideSplash(activity: Activity)
        {
            changeStatusBarColor(activity,
                                 ContextCompat.getColor(activity, R.color.colorPrimaryDark))
            val rippleBackground = activity.findViewById<RippleBackground>(R.id.rippleBackground)
            rippleBackground.stopRippleAnimation()
            splashScreen.decrement()
        }

        /**
         * Changes the status bar's color (only works on API 21+)
         *
         * @param activity: current running activity
         * @param color: the selected color for the status bar
         */
        fun changeStatusBarColor(activity: Activity, color: Int)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                val window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = color
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?)
    {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onStart()
    {
        super.onStart()
        tryRedirectToLogin()
    }

    override fun onResume()
    {
        super.onResume()
        tryRedirectToLogin()
        isActivityRunning = true
        setVisible(true)
    }

    override fun onPause()
    {
        super.onPause()
        isActivityRunning = false
        setVisible(false)
    }

    private fun tryRedirectToLogin()
    {
        if (user == null)
        {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * @return [this] as pixels on the current screen
     */
    fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    /**
     * Shows a highlighted text (colored in yellow) in a snackbar.
     * Used mainly to notify the user about errors
     *
     * @param root: the context's root element (activity's root layout)
     * @param msg:  the message displayed in the snackbar
     */
    protected open fun makeHighlightedSnackbar(root: View, msg: String)
    {
        val snackbar = Snackbar.make(root, msg, Snackbar.LENGTH_SHORT)
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(Color.YELLOW)
        snackbar.show()
    }

    /**
     * Changes the status bar's color (only works on API 21+)
     *
     * @param color: the selected color for the status bar
     */
    protected fun changeStatusBarColor(color: Int)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    /**
     * Blocks user interaction and displays a progress dialog.
     * After a timeout, progress dialog is dismissed and an alert dialog pops up to inform the user
     * about an error.
     * This method should be invoked when some asynchronous task is running in the background
     *
     * @param msg:     message to display to the user while loading
     * @param timeout: amount of time (in milliseconds) before the progress dialog is dismissed
     */
    protected open fun startLoading(msg: String?, timeout: Int?)
    {
        progress = ProgressDialog(this)
        progress!!.setCancelable(false)
        progress!!.isIndeterminate = true
        progress!!.setMessage(msg)
        progress!!.show()
        if (progressHandler == null)
        {
            progressHandler = Handler()
        }
        progressHandler!!.removeCallbacks(loadTimeoutTask)
        val loadTime =
            timeout ?: loadTimeout
        progressHandler!!.postDelayed(loadTimeoutTask, loadTime.toLong())
    }

    /**
     * Dismisses a loading progress dialog. Should be called after [startLoading]
     */
    protected open fun stopLoading()
    {
        if (progress == null || !progress!!.isShowing) return
        progress!!.dismiss()
        progressHandler?.removeCallbacks(loadTimeoutTask)
    }

    /**
     * Changes the activity's layout to the splash screen's.
     * The splash's screen must be *included* into the activity's layout.
     * This should be called when there's a background task that starts a new activity when finished.
     *
     * @param loadMessage: the message to be displayed on the splash screen
     */
    fun showSplash(loadMessage: String)
    {
        splashScreen.increment()
        try
        {
            val rippleBackground = this.findViewById<RippleBackground>(R.id.rippleBackground)
            val splashText = this.findViewById<TextView>(R.id.splashText)
            changeStatusBarColor(this, ContextCompat.getColor(this, R.color.backgroundSplash))
            splashText.text = loadMessage
            rippleBackground.startRippleAnimation()

        }
        catch (e: Exception)
        {
            splashScreen.decrement()
        }
    }

    /**
     * Cancels the splash's animation and returns the status bar color back to its default color
     */
    fun hideSplash()
    {
        changeStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark))
        val rippleBackground = this.findViewById<RippleBackground>(R.id.rippleBackground)
        rippleBackground.stopRippleAnimation()
        splashScreen.decrement()
    }
}