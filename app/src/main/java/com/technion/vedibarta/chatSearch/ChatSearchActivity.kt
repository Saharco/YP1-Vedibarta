package com.technion.vedibarta.chatSearch

import android.graphics.Typeface
import kotlinx.android.synthetic.main.activity_chat_search.*
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_search.editTabs
import kotlinx.android.synthetic.main.activity_chat_search.toolbar

class ChatSearchActivity : VedibartaActivity() {

    private val TAG = "ChatSearchActivity"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    var chosenCharacteristics = mutableSetOf<String>()
    var chosenSchool = ""
    var chosenRegion = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
        Log.d(TAG, "ChatSearchActivity Created")
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        setupViewPager(searchUserContainer)
        editTabs.setupWithViewPager(searchUserContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(SearchCharacteristicsFragment(), "1")
        adapter.addFragment(SearchExtraOptionsFragment(), "2")
        viewPager.adapter = adapter
    }

    override fun onBackPressed() {

    }
}
