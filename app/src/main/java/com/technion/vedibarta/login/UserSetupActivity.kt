package com.technion.vedibarta.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.CustomViewPager
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
        changeStatusBarColor(this, R.color.colorBoarding)
        doneButton.bringToFront()
        doneButton.setOnClickListener { onDoneClick() }
        editTabs.touchables.forEach { it.isEnabled = false }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putSerializable(STUDENT_KEY, setupStudent)
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(ChooseGenderFragment(), "1")
        adapter.addFragment(ChooseCharacteristicsFragment(), "2")
        adapter.addFragment(ChooseHobbiesFragment(), "3")
        val toast = Toast.makeText(
            applicationContext,
            R.string.user_setup_dialog_message,
            Toast.LENGTH_SHORT
        )
        viewPager.setPagingEnabled(false)
        viewPager.setOnInterceptTouchEventCustomBehavior {
            if (setupStudent.gender != Gender.NONE) {
                adapter.getItem(1).onStart()
                viewPager.setPagingEnabled(true)
                viewPager.setOnInterceptTouchEventCustomBehavior {  }
            } else {
                if(!toast.view.isShown)
                    toast.show()
            }
        }

        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
    }

    override fun onBackPressed() {

    }

    private fun onDoneClick(){
        if (validateUserInput()) {
            setupStudent.name = "$chosenFirstName $chosenLastName"
            database.students().userId().build().set(setupStudent)
                .addOnSuccessListener {
                    student = setupStudent
                    startActivity(Intent(this, UserProfileActivity::class.java))
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

    private fun missingDetailsDialog() {
        val message = SpannableStringBuilder()
            .bold { append(missingDetailsText).setSpan(RelativeSizeSpan(1f), 0, missingDetailsText.length, 0) }

        val text = resources.getString(R.string.user_setup_missing_details_dialog_title)

        val titleText = SpannableStringBuilder()
            .bold { append(text).setSpan(RelativeSizeSpan(1.2f),0,text.length,0) }
        titleText.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),0,text.length,0)

        val positiveButtonText = SpannableStringBuilder()
            .bold { append(resources.getString(R.string.ok)) }

        val builder = AlertDialog.Builder(this)
        builder
            .setTitle(titleText)
            .setIcon(ContextCompat.getDrawable(this,R.drawable.ic_error))
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> }

        builder.create().show()
    }

    private fun validateUserInput(): Boolean {

        missingDetailsText = ""
        val studentsCharacteristics = setupStudent.characteristics.filter { it.value }.keys

        if (setupStudent.gender == Gender.NONE) {
            missingDetailsText += "${resources.getString(R.string.user_setup_gender_missing)}\n"
            return false
        }

        Log.d(TAG, "first: $chosenFirstName last: $chosenLastName")

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
}
