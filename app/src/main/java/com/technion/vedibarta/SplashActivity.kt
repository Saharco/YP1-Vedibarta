package com.technion.vedibarta

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : VedibartaActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        changeStatusBarColor(ContextCompat.getColor(this, R.color.backgroundSplash))
        rippleBackground.startRippleAnimation()
    }
}
