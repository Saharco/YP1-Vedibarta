package com.technion.vedibarta.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
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
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*
import java.sql.Timestamp

class UserSetupActivity : VedibartaActivity() {

    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    private var missingDetailsText = ""

    var setupStudent = Student(
        uid = userId!!,
        lastActivity = Timestamp(System.currentTimeMillis())
    )

    lateinit var schoolsName: Array<String>
    lateinit var regionsName: Array<String>
    lateinit var schoolTags: Array<Int>

    var chosenFirstName = ""
    var chosenLastName = ""

    companion object {
        const val STUDENT_KEY = "student"
        private const val TAG = "UserSetupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if (savedInstanceState?.get(STUDENT_KEY) != null) {
            setupStudent = savedInstanceState[STUDENT_KEY] as Student
        }

        schoolsName = resources.getStringArray(R.array.schoolNameList)
        regionsName =
            resources.getStringArray(R.array.regionNameList).toList().distinct().toTypedArray()
        schoolTags = resources.getIntArray(R.array.schoolTagList).toTypedArray()

        setupViewPager(userSetupContainer)
        editTabs.setupWithViewPager(userSetupContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        editTabs.touchables.forEach { it.isEnabled = false }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putSerializable(STUDENT_KEY, setupStudent)
        super.onSaveInstanceState(outState)
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(ChooseGenderFragment(), "1")
        adapter.addFragment(ChooseExtraOptionsFragment(), "2")
        adapter.addFragment(ChooseCharacteristicsFragment(), "3")
        adapter.addFragment(ChooseHobbiesFragment(), "4")

        viewPager.setOnTouchListener { v, event ->
            if (setupStudent.gender == Gender.NONE) {
                Toast.makeText(
                    applicationContext,
                    R.string.user_setup_dialog_message,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                adapter.replaceFragment(2, ChooseCharacteristicsFragment())
                v.onTouchEvent(event)
            }
            return@setOnTouchListener true
        }

        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
//        viewPager.addOnPageChangeListener(CustomViewPageListener(this))
    }

    override fun onBackPressed() {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_setup_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionDoneSetup -> {
                if (validateUserInput()) {
                    setupStudent.name = "$chosenFirstName $chosenLastName"
                    database.students().userId().build().set(setupStudent)
                        .addOnSuccessListener {
                            student = setupStudent
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG)
                                .show()
                        }
                } else {
                    missingDetailsDialog()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun missingDetailsDialog() {
        val title = TextView(this)
        val message = Html.fromHtml("<b><small>$missingDetailsText</small></b>")
        title.setText(R.string.user_setup_missing_details_dialog_title)
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

    private fun validateUserInput(): Boolean {

        missingDetailsText = ""
        val studentsCharacteristics = setupStudent.characteristics.filter { it.value }.keys

        if (setupStudent.gender == Gender.NONE) {
            missingDetailsText += "${resources.getString(R.string.user_setup_gender_missing)}\n"
            return false
        }

        Log.d(TAG,"first: $chosenFirstName last: $chosenLastName")

        if (chosenFirstName == "") {
            missingDetailsText += "${resources.getString(R.string.user_setup_first_name_missing)}\n"
            return false
        }

        if (chosenLastName == "") {
            missingDetailsText += "${resources.getString(R.string.user_setup_last_name_missing)}\n"
            return false
        }

        if (!schoolsName.contains(setupStudent.school)) {
            missingDetailsText += "${resources.getString(R.string.user_setup_school_missing)}\n"
            return false
        }

        if (!regionsName.contains(setupStudent.region)) {
            missingDetailsText += "${resources.getString(R.string.user_setup_region_missing)}\n"
            return false
        }

        if (studentsCharacteristics.isEmpty()) {
            missingDetailsText += "${resources.getString(R.string.user_setup_characteristics_missing)}\n"
            return false
        }

        if (setupStudent.hobbies.isEmpty()) {
            missingDetailsText += "${resources.getString(R.string.user_setup_hobbies_missing)}\n"
            return false
        }

        return true
    }
/*
    inner class CustomViewPageListener(val activity: UserSetupActivity) :
        ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when (position) {
                0 -> {
                    activity.toolbarTitle.text =
                        activity.resources.getString(R.string.user_setup_title)
                }
                1 -> {
                    activity.toolbarTitle.text =
                        activity.resources.getString(R.string.user_setup_extra_options_title)
                }
                2 -> {
                    activity.toolbarTitle.text =
                        activity.resources.getString(R.string.user_setup_characteristics_title)
                }
                3 -> {
                    activity.toolbarTitle.text =
                        activity.resources.getString(R.string.user_setup_hobbies_title)
                }
            }
        }
    }

 */

}
