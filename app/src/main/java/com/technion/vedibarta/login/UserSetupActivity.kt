package com.technion.vedibarta.login

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*
import java.sql.Timestamp

class UserSetupActivity : VedibartaActivity() {

    private val TAG = "UserSetupActivity"
    private val STUDENT_KEY = "student"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    var setupStudent = Student(
        "",
        null,
        "",
        "",
        Gender.NONE,
        Timestamp(System.currentTimeMillis()),
        characteristics = arrayOf(),
        hobbies = arrayOf()
    )

    lateinit var schoolsName: Array<String>
    lateinit var regionsName: Array<String>
    lateinit var schoolTags: Array<Int>

    var chosenCharacteristics = mutableSetOf<String>()
    var chosenHobbies = mutableSetOf<String>()
    var chosenFirstName = ""
    var chosenLastName = ""
    var chosenSchool = ""
    var chosenRegion = ""
    var chosenGender = Gender.NONE

    var flag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if (savedInstanceState?.get(STUDENT_KEY) != null) {
            setupStudent = savedInstanceState[STUDENT_KEY] as Student
            flag = savedInstanceState.getBoolean("FLAG")
        }

        setupViewPager(userSetupContainer)
        editTabs.setupWithViewPager(userSetupContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        editTabs.touchables.forEach { it.isEnabled = false }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "Saving Student")
        Log.d(TAG, "Student Gender: ${setupStudent.gender}")

        outState.putSerializable(STUDENT_KEY, setupStudent)
        outState.putBoolean("FLAG", flag)
        super.onSaveInstanceState(outState)
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(ChooseGenderFragment(), "1")
        if (!flag) {
            adapter.addFragment(ChooseExtraOptionsFragment(), "2")
            adapter.addFragment(ChooseCharacteristicsFragment(), "3")
            adapter.addFragment(ChooseHobbiesFragment(), "4")
        }
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(CustomViewPageListener(this))
    }

    override fun onBackPressed() {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_setup_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO: Add checks that user have chosen all required items and filled all fields
            R.id.actionDoneSetup -> {
                if (validateUserInput()) {
                    startActivity(Intent(this, MainActivity::class.java))
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
        title.setText(R.string.user_setup_missing_details_dialog_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(resources.getColor(R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)
        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
            .setMessage(R.string.user_setup_missing_details_message)
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .show()
        builder.create()
    }

    private fun validateUserInput(): Boolean {

        Log.d(TAG, "Chars: ${setupStudent.characteristics.isNotEmpty()}, hobbies: ${setupStudent.hobbies.isNotEmpty()}, first name: $chosenFirstName ")
        Log.d(TAG, "last name: $chosenLastName,  School: ${setupStudent.school}, Region: ${setupStudent.region}")

        return setupStudent.characteristics.isNotEmpty() && setupStudent.hobbies.isNotEmpty() && chosenFirstName != ""
                && chosenLastName != "" && schoolsName.contains(setupStudent.school) && regionsName.contains(setupStudent.region)
    }

    class CustomViewPageListener(val activity: UserSetupActivity) :
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

}
