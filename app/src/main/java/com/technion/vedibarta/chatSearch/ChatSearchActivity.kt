package com.technion.vedibarta.chatSearch

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatCandidates.ChatCandidatesActivity
import com.technion.vedibarta.database.DatabaseVersioning
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

    var chosenSchool: String? = null
    var chosenRegion: String? = null

    var fakeStudent: Student = Student()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
        Log.d(TAG, "ChatSearchActivity Created")
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if(savedInstanceState != null){
            fakeStudent = savedInstanceState.getSerializable("STUDENT") as Student
            chosenSchool = savedInstanceState.getString("SCHOOL")
            chosenRegion = savedInstanceState.getString("REGION")
        }

        schoolsName = resources.getStringArray(R.array.schoolNameList)
        regionsName = resources.getStringArray(R.array.regionNameList).toList().distinct().toTypedArray()
        schoolTags = resources.getIntArray(R.array.schoolTagList).toTypedArray()

        setupViewPager(searchUserContainer)
        editTabs.setupWithViewPager(searchUserContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("STUDENT",fakeStudent)
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
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun displayErrorMessage(message: String) {
        val title = TextView(this)
        title.setText(R.string.chat_search_wrong_details_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(resources.getColor(R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)
        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .show()
        builder.create()
    }

    private fun displayIllegalRegion() {
        displayErrorMessage(getString(R.string.chat_search_wrong_region_message))
    }

    private fun displayIllegalSchool() {
        displayErrorMessage(getString(R.string.chat_search_wrong_school_message))
    }

    private fun displayNoCharacteristicsChosen() {
        displayErrorMessage(getString(R.string.chat_search_no_characteristics_chosen_message))
    }

    private fun validateChosenDetails() : Boolean{
        when {
            !regionsName.contains(chosenRegion) && chosenRegion != null -> displayIllegalRegion()
            !schoolsName.contains(chosenSchool) && chosenSchool != null -> displayIllegalSchool()
            fakeStudent.characteristics.isEmpty() -> displayNoCharacteristicsChosen()
            else -> return true
        }
        return false
    }

    private fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(SearchCharacteristicsFragment(), "1")
        adapter.addFragment(SearchExtraOptionsFragment(), "2")
        viewPager.adapter = adapter
    }

    private fun searchMatch() {
        Log.d(TAG, "Searching a match")

        val studentsCollection = DatabaseVersioning.currentVersion.instance.collection("students")

        MatcherImpl(studentsCollection, fakeStudent.characteristics.keys, chosenRegion, chosenSchool).match()
            .addOnSuccessListener(this) { students ->
                val filteredStudents = students.filter { it.uid != student!!.uid }
                if (filteredStudents.isNotEmpty()) {
                    Log.d(TAG, "Matched students successfully")

                    val intent = Intent(this, ChatCandidatesActivity::class.java)
                    intent.putExtra("STUDENTS", filteredStudents.toTypedArray())
                    startActivity(intent)
                    finish()
                } else {
                    Log.d(TAG, "No matching students founds")
                    Toast.makeText(this, R.string.no_matching_students, Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener(this) { exp ->
                Log.w(TAG, "Matching students failed", exp)
                if(!isInternetAvailable(this)) {
                    loadTimeoutTask.run()
                }
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
    }

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }
}
