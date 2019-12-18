package com.technion.vedibarta

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.login.UserSetupActivity
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * This activity is meant to show up as a transition when performing a load operation before opening an activity
 */
class SplashActivity : VedibartaActivity() {

    companion object {
        const val SPLASH_EXTRA_DURATION = 1200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        changeStatusBarColor(ContextCompat.getColor(this, R.color.backgroundSplash))
        rippleBackground.startRippleAnimation()

        //TODO: extend this mechanism by using the intent's passed values for "loadSuccess" and "loadFailed"
        tryLoadingUser()
    }

    private fun tryLoadingUser() {
        //TODO: need to add onFailureListener and enter a loop here
        database.students().userId().build().get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                student = document.toObject(Student::class.java)

                Handler().postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, SPLASH_EXTRA_DURATION)
            } else {
                startActivity(Intent(this, UserSetupActivity::class.java))
                finish()
            }
        }
    }
}
