package com.technion.vedibarta.chatSearch

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.vedibarta.R
import com.technion.vedibarta.chatCandidates.ChatCandidatesActivity
import com.technion.vedibarta.studentsMatching.impl.MatcherImpl
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_search.*

class ChatSearchActivity : VedibartaActivity() {

    companion object {
        private const val MATCHING_TIMEOUT = 10L
        private const val TAG = "ChatSearchActivity"
    }

    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    lateinit var schoolsName: Array<String>
    lateinit var regionsName: Array<String>
    lateinit var schoolTags: Array<Int>

    var chosenCharacteristics = arrayListOf<String>()
    var chosenSchool: String? = null
    var chosenRegion: String? = null

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
                    searchMatch()
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
        return (schoolsName.contains(chosenSchool)  || chosenSchool == null)
                && (regionsName.contains(chosenRegion) || chosenRegion == null)
    }

    private fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(SearchCharacteristicsFragment(), "1")
        adapter.addFragment(SearchExtraOptionsFragment(), "2")
        viewPager.adapter = adapter
    }

    private fun searchMatch() {
        Log.d(TAG, "Searching a match")

        val studentsCollection = FirebaseFirestore.getInstance().collection("students")

        MatcherImpl(studentsCollection, chosenCharacteristics, chosenRegion, chosenSchool).match()
            .addOnSuccessListener(this) { students ->
                if (students.isNotEmpty()) {
                    Log.d(TAG, "Matched students successfully")

                    val intent = Intent(this, ChatCandidatesActivity::class.java)
                    intent.putExtra("STUDENTS", students.toTypedArray())
                    startActivity(intent)
                    finish()
                } else {
                    Log.d(TAG, "No matching students founds")
                    Toast.makeText(this, R.string.no_matching_students, Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener(this) { exp ->
                Log.w(TAG, "Matching students failed", exp)
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
    }
}
