package com.technion.vedibarta.userProfile

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_profile_edit.*
import androidx.viewpager.widget.ViewPager

class ProfileEditActivity : VedibartaActivity() {

    private val TAG = "ProfileEditActivity"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        Log.d(TAG, "created ProfileEditActivity")

        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            editProfileContainer.setPagingEnabled(false)
        setupViewPager(editProfileContainer)

        editTabs.setupWithViewPager(editProfileContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(ProfileEditCharacteristicsFragment(), "מאפייני זהות")
        adapter.addFragment(ProfileEditHobbiesFragment(), "תחביבים")
        viewPager.adapter = adapter
    }
}