package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.VedibartaActivity

class TeacherMainActivity : VedibartaActivity() {

    private val navController by lazy { findNavController(R.id.main_content) }
    private val bottomNavigation by lazy { findViewById<BottomNavigationView>(R.id.bottomNavigationView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)
        bottomNavigation.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content)
        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
        (fragment as? OnBackPressed)?.onBackPressed()?.not()?.let {
            if (it)
                super.onBackPressed()
        }
    }
    interface OnBackPressed {
        fun onBackPressed(): Boolean
    }
}

