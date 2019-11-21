package com.technion.vedibarta.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.technion.vedibarta.R






class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val fm = supportFragmentManager
        fm.beginTransaction().apply {
            replace(R.id.login_screen_fragment, LoginOptionsFragment())
        }.commit()
    }
}
