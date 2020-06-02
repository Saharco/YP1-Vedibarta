package com.technion.vedibarta.teacher

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.technion.vedibarta.R

class TeacherMainActivity : FragmentActivity() {
    lateinit var bottomNavigation: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)
        openFragment(TeacherHomeFragment.newInstance(""))
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> openFragment(TeacherHomeFragment.newInstance("ודיברת"))
                R.id.profile -> openFragment(TeacherProfileFragment.newInstance("",""))
                R.id.search -> openFragment(TeacherCharacteristicsFragment.newInstance("", "")) //openFragment(TeacherSearchExtraOptionsFragment.newInstance("", ""))
                R.id.chat -> openFragment(TeacherHomeFragment.newInstance("השיחות שלי")) // Should open teacher chats
                R.id.reports -> openFragment(TeacherHomeFragment.newInstance("דיווחים")) //Should open teacher reports
            }
            true
        }
    }

    fun openFragment(fragment: Fragment?) {
        if (fragment != null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

}

