package com.technion.vedibarta.chatSearch

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatCandidates.ChatCandidatesActivity
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.studentsMatching.impl.MatcherImpl
import com.technion.vedibarta.fragments.CharacteristicsFragment
import com.technion.vedibarta.userProfile.CharacteristicsFragment
import com.technion.vedibarta.matching.StudentsMatcher
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.extensions.isInForeground
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.Resource
import kotlinx.android.synthetic.main.activity_chat_search.*

class ChatSearchActivity : VedibartaActivity(), VedibartaFragment.ArgumentTransfer {

    companion object {
        private const val MATCHING_TIMEOUT = 10L
        private const val MINIMUM_TRANSITION_TIME = 900L
        private const val TAG = "ChatSearchActivity"
    }

    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    lateinit var schoolsNameTask: Task<out Resource>
    lateinit var regionsNameTask: Task<out Resource>
    lateinit var schoolTags: Array<Int>

    lateinit var characteristicsTask : Task<MultilingualResource>

    var chosenSchool: String? = null
    var chosenRegion: String? = null

    var fakeStudent: Student =
        Student(gender = student!!.gender)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
        Log.d(TAG, "ChatSearchActivity Created")
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if (savedInstanceState != null) {
            fakeStudent = savedInstanceState.getSerializable("STUDENT") as Student
            chosenSchool = savedInstanceState.getString("SCHOOL")
            chosenRegion = savedInstanceState.getString("REGION")
        }

        characteristicsTask = RemoteResourcesManager(this).findMultilingualResource("characteristics", Gender.NONE)



        schoolsNameTask = RemoteResourcesManager(this).findResource("schools")
        regionsNameTask = RemoteResourcesManager(this).findResource("regions")
        schoolTags = resources.getIntArray(R.array.schoolTagList).toTypedArray()

        setupViewPager(searchUserContainer)
        editTabs.setupWithViewPager(searchUserContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        viewFlipper.setInAnimation(this, android.R.anim.fade_in)
        viewFlipper.setOutAnimation(this, android.R.anim.fade_out)
    }

    override fun onStop() {
        super.onStop()
        if (!splashScreen.isIdleNow)
            splashScreen.decrement()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("STUDENT", fakeStudent)
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
                validateChosenDetails()
                    .addOnSuccessListener(this){
                        if(it)
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

    private fun validateChosenDetails(): Task<Boolean> {
        return Tasks.whenAll(regionsNameTask, schoolsNameTask)
            .continueWith {
                when {
                    !regionsNameTask.result!!.getAll().contains(chosenRegion) && chosenRegion != null -> displayIllegalRegion()
                    !schoolsNameTask.result!!.getAll().contains(chosenSchool) && chosenSchool != null -> displayIllegalSchool()
                    fakeStudent.characteristics.isEmpty() -> displayNoCharacteristicsChosen()
                    else -> return@continueWith true
                }
                return@continueWith false
            }
    }

    private fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(CharacteristicsFragment(), "1")
        adapter.addFragment(SearchExtraOptionsFragment(), "2")
        viewPager.adapter = adapter
    }

    private fun searchMatch() {
        Log.d(TAG, "Searching a match")

        StudentsMatcher().match(
            fakeStudent.characteristics.keys,
            chosenRegion,
            chosenSchool
        ).addOnSuccessListener(this) { students ->
            val filteredStudents = students.filter { it.uid != student!!.uid }
            if (filteredStudents.isNotEmpty()) {
                Log.d(TAG, "Matched students successfully")

                val intent = Intent(this, ChatCandidatesActivity::class.java)
                intent.putExtra("STUDENTS", filteredStudents.toTypedArray())

                Handler().postDelayed({
                    if (this@ChatSearchActivity.isInForeground()) {
                        startActivity(intent)
                        finish()
                    }
                }, MINIMUM_TRANSITION_TIME)

            } else {
                Log.d(TAG, "No matching students found")
                Toast.makeText(this, R.string.no_matching_students, Toast.LENGTH_LONG).show()
                if (this@ChatSearchActivity.isInForeground()) {
                    hideSplash()
                    viewFlipper.showPrevious()
                }
            }
        }.addOnFailureListener(this) { exp ->
            Log.w(TAG, "Matching students failed", exp)
            if (this@ChatSearchActivity.isInForeground()) {
                if (isInternetAvailable(this@ChatSearchActivity))
                //TODO: retry search!
                else
                    onBackPressed()
            }
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        }

        viewFlipper.showNext()
        showSplash(getString(R.string.chat_search_loading_message))
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

    override fun getArgs(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["student"] = fakeStudent
        map["characteristicsTask"] = characteristicsTask
        map["schoolsNameTask"] = schoolsNameTask
        map["regionsNameTask"] = regionsNameTask
        map["activity"] = this
        return map
    }
}
