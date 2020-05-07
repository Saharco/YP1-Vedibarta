package com.technion.vedibarta.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.Loaded
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.data.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.fragments.HobbiesFragment
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import kotlinx.android.synthetic.main.activity_user_setup.*

class UserSetupActivity : VedibartaActivity(), VedibartaFragment.ArgumentTransfer {

    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    private lateinit var characteristicsNext: OnNextClickForCharacteristics

    private val viewModel: UserSetupViewModel by viewModels {
        userSetupViewModelFactory(
            applicationContext
        )
    }

    companion object {
        private const val TAG = "UserSetupActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        viewModel.resourcesMediator.observe(this, Observer {
            if (it) {
                loading.visibility = View.GONE
                layout.visibility = View.VISIBLE
            }
        })

        loading.visibility = View.VISIBLE
        layout.visibility = View.GONE

        setupViewPager(userSetupContainer)
        editTabs.setupWithViewPager(userSetupContainer)
        editTabs.touchables.forEach { it.isEnabled = false }

        changeStatusBarColor(this, R.color.colorBoarding)

        initButtons()
    }

    private fun initButtons() {
        doneButton.bringToFront()
        doneButton.setOnClickListener { onDoneClick() }
        nextButton.setOnClickListener { onNextClick() }
        backButton.setOnClickListener { onBackClick() }
        backButton.bringToFront()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        val characteristicsFragment = ChooseCharacteristicsFragment()
        characteristicsNext = characteristicsFragment as? OnNextClickForCharacteristics
            ?: throw ClassCastException("$characteristicsFragment must implement ${OnNextClickForCharacteristics::class}")
        adapter.addFragment(ChoosePersonalInfoFragment(), "1")
        adapter.addFragment(characteristicsFragment, "2")
        adapter.addFragment(HobbiesFragment(), "3")
        viewPager.setPagingEnabled(false)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
    }

    override fun onBackPressed() {
        onBackClick()
    }

    private fun onDoneClick() {
        when (val result = validateUserInput()) {
            is Success -> database.students().userId().build().set(result.student)
                .addOnSuccessListener {
                    student = result.student
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG)
                        .show()
                }
            is Failure -> missingDetailsDialog(result.msg)
        }
    }

    private fun onBackClick() {
        when (userSetupContainer.currentItem) {
            1 -> {
                if (characteristicsNext.onBackClick()) {
                    userSetupContainer.currentItem -= 1
                    backButton.visibility = View.GONE
                } else {
                    updateTitle(false)
                }
            }
            2 -> {
                userSetupContainer.currentItem -= 1
                nextButton.visibility = View.VISIBLE
            }
        }
    }

    private fun updateTitle(increase: Boolean) {
        (0..2).forEach {
            val title = editTabs.getTabAt(it)!!.text.toString().toInt()
            if (increase)
                editTabs.getTabAt(it)!!.text = title.inc().toString()
            else
                editTabs.getTabAt(it)!!.text = title.dec().toString()
        }
    }

    private fun onNextClick() {
        when (userSetupContainer.currentItem) {
            0 -> {
                if (viewModel.gender.value != Gender.NONE) {
                    (userSetupContainer.adapter as SectionsPageAdapter).notifyDataSetChanged()
                    userSetupContainer.currentItem += 1
                    backButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, R.string.user_setup_gender_missing, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            1 -> {
                if (characteristicsNext.onNextClick()) {
                    userSetupContainer.currentItem += 1
                    doneButton.visibility = View.VISIBLE
                    nextButton.visibility = View.GONE
                } else {
                    updateTitle(true)
                }
            }
        }
    }

    private fun missingDetailsDialog(msg: String) {
        val message = SpannableStringBuilder()
            .bold {
                append(msg).setSpan(
                    RelativeSizeSpan(1f),
                    0,
                    msg.length,
                    0
                )
            }

        val text = resources.getString(R.string.user_setup_missing_details_dialog_title)

        val titleText = SpannableStringBuilder()
            .bold { append(text).setSpan(RelativeSizeSpan(1.2f), 0, text.length, 0) }
        titleText.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length, 0)

        val positiveButtonText = SpannableStringBuilder()
            .bold { append(resources.getString(R.string.ok)) }

        val builder = AlertDialog.Builder(this)
        builder
            .setTitle(titleText)
            .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_error))
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> }

        builder.create().show()
    }

    private fun validateUserInput(): StudentResult {
        val gender = viewModel.gender
        val grade = viewModel.grade

        val studentHobbies = viewModel.chosenHobbies
        val studentsCharacteristics = viewModel.chosenCharacteristics

        val schoolNamesList = viewModel.schoolsName.value as Loaded
        val regionNamesList = viewModel.regionsName.value as Loaded

        if (gender.value == Gender.NONE)
            return Failure(resources.getString(R.string.user_setup_gender_missing))

        val firstName = when (val chosenFirstName = viewModel.chosenFirstName) {
            is Unfilled -> return Failure(resources.getString(R.string.user_setup_first_name_missing))
            is Filled -> chosenFirstName.text
        }

        val lastName = when (val chosenLastName = viewModel.chosenLastName) {
            is Unfilled -> return Failure(resources.getString(R.string.user_setup_last_name_missing))
            is Filled -> chosenLastName.text
        }

        val school = when (val chosenSchool = viewModel.chosenSchool) {
            is Unfilled -> return Failure(resources.getString(R.string.user_setup_school_missing))
            is Filled -> chosenSchool.text.substringBefore(" -")
        }


        if (!schoolNamesList.data.getAll().contains(school))
            return Failure(resources.getString(R.string.user_setup_school_missing))

        val region = when (val chosenRegion = viewModel.chosenRegion) {
            is Unfilled -> return Failure(resources.getString(R.string.user_setup_region_missing))
            is Filled -> chosenRegion.text
        }

        if (regionNamesList.data.getAll().contains(region))
            return Failure(resources.getString(R.string.user_setup_region_missing))

        if (!validateSchoolAndRegionExists(school, region)) {
            return Failure(resources.getString(R.string.user_setup_wrong_school_and_region_combination))
        }

        if (grade == Grade.NONE) {
            return Failure(resources.getString(R.string.user_setup_grade_missing))
        }

        if (studentsCharacteristics.isEmpty()) {
            return Failure(resources.getString(R.string.user_setup_characteristics_missing))
        }

        if (studentHobbies.isEmpty()) {
            return Failure(resources.getString(R.string.user_setup_hobbies_missing))
        }

        return Success(
            Student(
                uid = userId!!,
                name = "$firstName $lastName",
                gender = gender.value!!,
                region = region,
                school = school,
                grade = grade,
                characteristics = studentsCharacteristics,
                hobbies = studentHobbies
            )
        )
    }

    private fun validateSchoolAndRegionExists(school: String, region: String): Boolean {
        val schoolNamesList = viewModel.schoolsName.value as Loaded
        val regionNamesList = viewModel.regionsName.value as Loaded
        val schoolAndRegionMap =
            schoolNamesList.data.getAll().zip(regionNamesList.data.getAll()).toMap()

        return schoolAndRegionMap.containsKey(school) && schoolAndRegionMap[school] == region
    }

    override fun getArgs(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map["activity"] = this
        return map
    }

    fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> viewModel.grade = Grade.TENTH
            R.id.gradeEleventh -> viewModel.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> viewModel.grade = Grade.TWELFTH
            else -> {
            }
        }
    }

    interface OnNextClickForCharacteristics {
        fun onNextClick(): Boolean
        fun onBackClick(): Boolean
    }

}

sealed class StudentResult

data class Success(val student: Student) : StudentResult()

data class Failure(val msg: String) : StudentResult()
