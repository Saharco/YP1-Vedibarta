package com.technion.vedibarta.chatSearch

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.R
import com.technion.vedibarta.chatCandidates.ChatCandidatesActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_search.*

class ChatSearchActivity : VedibartaActivity() {

    private val TAG = "ChatSearchActivity"

    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    lateinit var schoolsName: Array<String>
    lateinit var regionsName: Array<String>
    lateinit var schoolTags: Array<Int>

    var chosenCharacteristics = arrayListOf<String>()
    var chosenSchool = ""
    var chosenRegion = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
        Log.d(TAG, "ChatSearchActivity Created")
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if(savedInstanceState != null){
            chosenCharacteristics = savedInstanceState.getStringArrayList("CHARS")!!
            chosenSchool = savedInstanceState.getString("SCHOOL")!!
            chosenRegion = savedInstanceState.getString("REGION")!!
        }

        setupViewPager(searchUserContainer)
        editTabs.setupWithViewPager(searchUserContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        searchButton.setOnClickListener {
            startActivity(Intent(this, ChatCandidatesActivity::class.java))
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("CHARS", chosenCharacteristics)
        outState.putString("SCHOOL", chosenSchool)
        outState.putString("REGION", chosenRegion)
        super.onSaveInstanceState(outState)
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionChatSearch -> {
                //TODO pass the chosen filters to database/other activity fo rmatching
                if(validateChosenDetails()) {
                    startActivity(Intent(this, ChatCandidatesActivity::class.java))
                    finish()
                }
                else{
                    missingDetailsDialog()

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun missingDetailsDialog() {
        val title = TextView(this)
        title.setText(R.string.chat_search_wrong_details_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(resources.getColor(R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)
        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
            .setMessage(R.string.chat_search_wrong_details_message)
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .show()
        builder.create()
    }

    private fun validateChosenDetails() : Boolean{
        return (schoolsName.contains(chosenSchool)  || chosenSchool == "")
                && (regionsName.contains(chosenRegion) || chosenRegion == "")
    }

    private fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(SearchCharacteristicsFragment(), "1")
        adapter.addFragment(SearchExtraOptionsFragment(), "2")
        viewPager.adapter = adapter
    }
}
