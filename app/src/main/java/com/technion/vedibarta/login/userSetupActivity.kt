package com.technion.vedibarta.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.notSwipeableViewPager
import kotlinx.android.synthetic.main.activity_user_setup.*

class userSetupActivity : VedibartaActivity() {

    private val TAG = "UserSetupActivity"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    var chosenCharacteristics = mutableSetOf<String>()
    var chosenHobbies = mutableSetOf<String>()
    var chosenGender = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        setupViewPager(userSetupContainer)
        editTabs.setupWithViewPager(userSetupContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        editTabs.touchables.forEach { it.isEnabled = false }
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        viewPager.setPagingEnabled(false)
        adapter.addFragment(chooseGenderFragment(), "1")
        adapter.addFragment(chooseCharacteristicsFragment(), "2")
        adapter.addFragment(chooseHobbiesFragment(), "3")
        viewPager.adapter = adapter
    }

    override fun onBackPressed() {

    }
}
