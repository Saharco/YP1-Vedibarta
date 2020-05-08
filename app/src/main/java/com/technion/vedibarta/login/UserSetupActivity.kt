package com.technion.vedibarta.login

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.HobbiesViewModel
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.data.viewModels.hobbiesViewModelFactory
import com.technion.vedibarta.data.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.fragments.HobbiesFragment
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*

class UserSetupActivity : VedibartaActivity(){

    private val userSetupViewModel: UserSetupViewModel by viewModels {
        userSetupViewModelFactory(
            applicationContext
        )
    }
    private val hobbiesViewModel: HobbiesViewModel by viewModels {
        hobbiesViewModelFactory(applicationContext)
    }
    companion object {
        private const val TAG = "UserSetupActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)
        userSetupViewModel.resourcesMediator.observe(this, Observer {observerHandler(it) })

        hobbiesViewModel.startLoading()

        if (userSetupViewModel.reachedLastPage)
            doneButton.visibility = View.VISIBLE
        if(userSetupViewModel.backButtonVisible)
            backButton.visibility = View.VISIBLE

        loading.visibility = View.VISIBLE
        layout.visibility = View.GONE
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

    private fun observerHandler(value: LoadableData<Unit>){
        when(value){
            is Loaded -> {
                loading.visibility = View.GONE
                layout.visibility = View.VISIBLE
                setupViewPager(userSetupContainer)
                TabLayoutMediator(editTabs, userSetupContainer){tab, position ->
                    tab.text = "${(position + 1)}"
                }.attach()
                editTabs.touchables.forEach { it.isEnabled = false }

                userSetupViewModel.resourcesMediator.removeObservers(this)
            }
            is Error -> Toast.makeText(this, value.reason, Toast.LENGTH_LONG).show()
            is SlowLoadingEvent -> if (!userSetupViewModel.slowLoadingEventHandled){
                Toast.makeText(this, resources.getString(R.string.net_error), Toast.LENGTH_SHORT).show()
            userSetupViewModel.slowLoadingEventHandled = true}
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val characteristicsFragmentList: List<Fragment> = (userSetupViewModel.characteristicsByCategory.value as Loaded).data
            .keys.map { ChooseCharacteristicsFragment(it) }

        val adapter = FragmentListStateAdapter(this, characteristicsFragmentList)
        adapter.addFragment(0, ChoosePersonalInfoFragment())
        adapter.addFragment(HobbiesFragment())
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter
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
                backButton.visibility = View.GONE
                userSetupViewModel.backButtonVisible = false
                userSetupContainer.currentItem -= 1
            }
            userSetupContainer.adapter!!.itemCount-1 -> {
                userSetupContainer.currentItem -= 1
                nextButton.visibility = View.VISIBLE
            }
            else -> userSetupContainer.currentItem -= 1
        }
    }

    private fun onNextClick() {
        when (userSetupContainer.currentItem) {
            0 -> {
                if (userSetupViewModel.gender.value != Gender.NONE) {
                    userSetupContainer.currentItem += 1
                    backButton.visibility = View.VISIBLE
                    userSetupViewModel.backButtonVisible = true
                } else {
                    Toast.makeText(this, R.string.user_setup_gender_missing, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            userSetupContainer.adapter!!.itemCount-2 -> {
                userSetupContainer.currentItem += 1
                userSetupViewModel.reachedLastPage = true
                doneButton.visibility = View.VISIBLE
                nextButton.visibility = View.GONE
            }
            else -> userSetupContainer.currentItem += 1
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
        val gender = userSetupViewModel.gender
        val grade = userSetupViewModel.grade

        val studentHobbies = hobbiesViewModel.chosenHobbies
        val studentsCharacteristics = userSetupViewModel.chosenCharacteristics

        val schoolNamesList = userSetupViewModel.schoolsName.value as Loaded
        val regionNamesList = userSetupViewModel.regionsName.value as Loaded

        if (gender.value == Gender.NONE)
            return Failure(resources.getString(R.string.user_setup_gender_missing))

        val firstName = when (val chosenFirstName = userSetupViewModel.chosenFirstName) {
            is Unfilled -> return Failure(resources.getString(R.string.user_setup_first_name_missing))
            is Filled -> chosenFirstName.text
        }

        val lastName = when (val chosenLastName = userSetupViewModel.chosenLastName) {
            is Unfilled -> return Failure(resources.getString(R.string.user_setup_last_name_missing))
            is Filled -> chosenLastName.text
        }

        val school = when (val chosenSchool = userSetupViewModel.chosenSchool) {
            is Unfilled -> return Failure(resources.getString(R.string.user_setup_school_missing))
            is Filled -> chosenSchool.text.substringBefore(" -")
        }


        if (!schoolNamesList.data.getAll().contains(school))
            return Failure(resources.getString(R.string.user_setup_school_missing))

        val region = when (val chosenRegion = userSetupViewModel.chosenRegion) {
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
        val schoolNamesList = userSetupViewModel.schoolsName.value as Loaded
        val regionNamesList = userSetupViewModel.regionsName.value as Loaded
        val schoolAndRegionMap =
            schoolNamesList.data.getAll().zip(regionNamesList.data.getAll()).toMap()

        return schoolAndRegionMap.containsKey(school) && schoolAndRegionMap[school] == region
    }

    fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> userSetupViewModel.grade = Grade.TENTH
            R.id.gradeEleventh -> userSetupViewModel.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> userSetupViewModel.grade = Grade.TWELFTH
            else -> {
            }
        }
    }

}

sealed class StudentResult

data class Success(val student: Student) : StudentResult()

data class Failure(val msg: String) : StudentResult()
