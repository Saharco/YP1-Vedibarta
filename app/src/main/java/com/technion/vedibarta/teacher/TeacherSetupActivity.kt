package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import kotlinx.android.synthetic.main.activity_user_setup.*

class TeacherSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_setup)
        setupViewPager(userSetupContainer)
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val adapter = FragmentListStateAdapter(this, mutableListOf({TeacherPersonalInfoFragment()}))
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter
        TabLayoutMediator(editTabs, userSetupContainer) { tab, position ->
            tab.text = "${(position + 1)}"
        }.attach()
        editTabs.touchables.forEach { it.isEnabled = false }
    }
}
