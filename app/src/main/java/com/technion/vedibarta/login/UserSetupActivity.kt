package com.technion.vedibarta.login

import android.annotation.SuppressLint
import android.app.ProgressDialog
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
import androidx.viewpager.widget.ViewPager
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*
import java.sql.Date
import java.sql.Timestamp

class UserSetupActivity : VedibartaActivity() {

    private val TAG = "UserSetupActivity"
    private val STUDENT_KEY = "student"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    private var missingDetailsText = ""


    var setupStudent = Student(
        lastActivity = Timestamp(System.currentTimeMillis())
    )

    lateinit var schoolsName: Array<String>
    lateinit var regionsName: Array<String>
    lateinit var schoolTags: Array<Int>

    var chosenFirstName = ""
    var chosenLastName = ""

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

//        val dialog = ProgressDialog(this).apply {
//            setMessage(getString(R.string.checking_document))
//            setCancelable(false)
//            setIndeterminate(true)
//            show()
//        }
//
//        database.students().userId().build().get().addOnSuccessListener { document ->
//            if (document != null && document.exists())
//            {
//                dialog.dismiss()
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//        }

        setContentView(R.layout.activity_user_setup)
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if (savedInstanceState?.get(STUDENT_KEY) != null) {
            setupStudent = savedInstanceState[STUDENT_KEY] as Student
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
                    "יש לבחור בן/בת",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                v.onTouchEvent(event)
            }
            return@setOnTouchListener true
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
            R.id.actionDoneSetup -> {
                if (validateUserInput()) {
                    setupStudent.name = "$chosenLastName $chosenLastName"
                    database.students().userId().build().set(setupStudent)
                        .addOnSuccessListener {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG) }
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

        Log.d(TAG,"Chars: ${studentsCharacteristics.isNotEmpty()}, hobbies: ${setupStudent.hobbies.isNotEmpty()}, first name: $chosenFirstName ")
        Log.d(TAG,"last name: $chosenLastName,  School: ${setupStudent.school}, Region: ${setupStudent.region}")

        if (setupStudent.gender == Gender.NONE) {
            missingDetailsText += "יש לבחור בן/בת\n"
            return false
        }

        if (chosenFirstName == "") {
            missingDetailsText += "יש למלא את השדה שם פרטי\n"
            return false
        }

        if (chosenLastName == "") {
            missingDetailsText += "יש למלא את השדה שם משפחה\n"
            return false
        }

        if (!schoolsName.contains(setupStudent.school)) {
            missingDetailsText += "יש לבחור בית ספר מהרשימה\n"
            return false
        }

        if (!regionsName.contains(setupStudent.region)) {
            missingDetailsText += "יש לבחור מקום מגורים מהרשימה\n"
            return false
        }

        if (studentsCharacteristics.isEmpty()) {
            missingDetailsText += "יש לבחור מאפייני זהות\n"
            return false
        }

        if (setupStudent.hobbies.isEmpty()) {
            missingDetailsText += "יש לבחור תחביבים\n"
            return false
        }

        return true
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
