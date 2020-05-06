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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.fragments.HobbiesFragment
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.extensions.executeAfterTimeoutInMillis
import com.technion.vedibarta.viewModels.UserSetupViewModel
import com.technion.vedibarta.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource
import kotlinx.android.synthetic.main.activity_user_setup.*
import kotlinx.android.synthetic.main.fragment_choose_personal_info.*

class UserSetupActivity : VedibartaActivity(), VedibartaFragment.ArgumentTransfer {

    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    private var missingDetailsText = ""

    var setupStudent = Student(
        uid = userId!!
    )

    private lateinit var characteristicsNext: OnNextClickForCharacteristics

    lateinit var hobbyCardTask: Task<List<HobbyCard>>
    lateinit var hobbiesResourceTask: Task<MultilingualTextResource>

    lateinit var characteristicsMaleTask: Task<MultilingualTextResource>
    lateinit var characteristicsFemaleTask: Task<MultilingualTextResource>
    private lateinit var characteristicsWithCategoriesMaleTask: Task<Map<String, Array<String>>>
    private lateinit var characteristicsWithCategoriesFemaleTask: Task<Map<String, Array<String>>>

    private val viewModel: UserSetupViewModel by viewModels { userSetupViewModelFactory(userId!!) }


    lateinit var schoolsNameTask: Task<out TextResource>
    lateinit var regionsNameTask: Task<out TextResource>

    var chosenFirstName = ""
    var chosenLastName = ""

    companion object {
        const val STUDENT_KEY = "student"
        const val FIRST_NAME_KEY = "firstname"
        const val LAST_NAME_KEY = "lastname"
        private const val TAG = "UserSetupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)


        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        if (savedInstanceState?.get(STUDENT_KEY) != null) {
            setupStudent = savedInstanceState[STUDENT_KEY] as Student
        }

        if (savedInstanceState?.get(FIRST_NAME_KEY) != null)
            chosenFirstName = savedInstanceState.getString(FIRST_NAME_KEY)!!

        if (savedInstanceState?.get(LAST_NAME_KEY) != null)
            chosenLastName = savedInstanceState.getString(LAST_NAME_KEY)!!

        loading.visibility = View.VISIBLE
        layout.visibility = View.GONE

        loadResources()

        Tasks.whenAll(
            schoolsNameTask,
            regionsNameTask,
            characteristicsMaleTask,
            characteristicsFemaleTask,
            characteristicsWithCategoriesFemaleTask,
            characteristicsWithCategoriesMaleTask
        )
            .executeAfterTimeoutInMillis(5000L) {
                internetConnectionErrorHandler(this)
            }
            .addOnSuccessListener(this) {
                loading.visibility = View.GONE
                layout.visibility = View.VISIBLE
            }

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

    private fun loadResources() {
        hobbiesResourceTask = RemoteTextResourcesManager(this)
            .findMultilingualResource("hobbies/all")

        hobbyCardTask = VedibartaFragment.loadHobbies(this)

        characteristicsMaleTask =
            RemoteTextResourcesManager(this).findMultilingualResource(
                "characteristics/all",
                Gender.MALE
            )
        characteristicsFemaleTask =
            RemoteTextResourcesManager(this).findMultilingualResource(
                "characteristics/all",
                Gender.FEMALE
            )

        characteristicsWithCategoriesMaleTask =
            VedibartaFragment.loadCharacteristics(this, Gender.MALE)
        characteristicsWithCategoriesFemaleTask =
            VedibartaFragment.loadCharacteristics(this, Gender.FEMALE)

        schoolsNameTask = RemoteTextResourcesManager(this).findResource("schools")
        regionsNameTask = RemoteTextResourcesManager(this).findResource("regions")
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putSerializable(STUDENT_KEY, setupStudent)
        outState.putString(FIRST_NAME_KEY, chosenFirstName)
        outState.putString(LAST_NAME_KEY, chosenLastName)
        super.onSaveInstanceState(outState)
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
        validateUserInput()
            .addOnSuccessListener(this) {StudentResult ->
                when(StudentResult){
                    is Success -> database.students().userId().build().set(StudentResult.student)
                        .addOnSuccessListener {
                            student = StudentResult.student
                            startActivity(Intent(this, UserProfileActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG)
                                .show()
                        }
                    is Failure -> missingDetailsDialog(StudentResult.msg)
                }
            }
    }

    private fun onBackClick() {
        when (userSetupContainer.currentItem) {
            1 -> {
                characteristicsNext.onBackClick()
                    .onSuccessTask {
                        if (it == true) {
                            userSetupContainer.currentItem -= 1
                            backButton.visibility = View.GONE
                        } else {
                            updateTitle(false)
                        }
                        Tasks.call { }
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
                if (setupStudent.gender != Gender.NONE) {
                    (userSetupContainer.adapter as SectionsPageAdapter).notifyDataSetChanged()
                    userSetupContainer.currentItem += 1
                    backButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, R.string.user_setup_gender_missing, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            1 -> {
                characteristicsNext.onNextClick()
                    .onSuccessTask {
                        if (it == true) {
                            userSetupContainer.currentItem += 1
                            doneButton.visibility = View.VISIBLE
                            nextButton.visibility = View.GONE
                        } else {
                            updateTitle(true)
                        }
                        Tasks.call { }
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

    private fun validateUserInput(): Task<StudentResult> {
        return Tasks.whenAll(
            characteristicsFemaleTask,
            characteristicsMaleTask,
            schoolsNameTask,
            regionsNameTask,
            hobbiesResourceTask,
            hobbyCardTask
        )
            .continueWith {
//                missingDetailsText = ""
//                val studentsCharacteristics = setupStudent.characteristics.filter { it.value }.keys

                val chosenFirstName = viewModel.chosenFirstName
                val chosenLastName = viewModel.chosenLastName
                val gender = viewModel.gender
                val grade = viewModel.grade
                val chosenSchool = viewModel.chosenSchool
                val chosenRegion = viewModel.chosenRegion
                val studentHobbies = viewModel.chosenHobbies
                val studentsCharacteristics = viewModel.chosenCharacteristics

                if (gender == Gender.NONE)
                    return@continueWith Failure(resources.getString(R.string.user_setup_gender_missing))

                val firstName = when (chosenFirstName) {
                    is Unfilled -> return@continueWith Failure(resources.getString(R.string.user_setup_first_name_missing))
                    is Filled -> chosenFirstName.text
                }

                val lastName = when (chosenLastName) {
                    is Unfilled -> return@continueWith Failure(resources.getString(R.string.user_setup_last_name_missing))
                    is Filled -> chosenFirstName.text
                }

                val school = when (chosenSchool) {
                    is Unfilled -> return@continueWith Failure(resources.getString(R.string.user_setup_school_missing))
                    is Filled -> chosenSchool.text.substringBefore(" -")
                }

                if (!schoolsNameTask.result!!.getAll().contains(school))
                    return@continueWith Failure(resources.getString(R.string.user_setup_school_missing))

                val region = when (chosenRegion) {
                    is Unfilled -> return@continueWith Failure(resources.getString(R.string.user_setup_region_missing))
                    is Filled -> chosenRegion.text
                }

                if (regionsNameTask.result!!.getAll().contains(region))
                    return@continueWith Failure(resources.getString(R.string.user_setup_region_missing))

                if (!validateSchoolAndRegionExists(school, region)) {
                    return@continueWith Failure(resources.getString(R.string.user_setup_wrong_school_and_region_combination))
                }

                if (setupStudent.grade == Grade.NONE) {
                    return@continueWith Failure(resources.getString(R.string.user_setup_grade_missing))
                }

                if (studentsCharacteristics.isEmpty()) {
                    return@continueWith Failure(resources.getString(R.string.user_setup_characteristics_missing))
                }

                if (studentHobbies.isEmpty()) {
                    return@continueWith Failure(resources.getString(R.string.user_setup_hobbies_missing))
                }

                return@continueWith Success(
                    Student(
                        uid = viewModel.uid,
                        name = "$firstName $lastName",
                        gender = gender,
                        region = region,
                        school = school,
                        grade = grade,
                        characteristics = studentsCharacteristics,
                        hobbies = studentHobbies
                    )
                )
            }
    }

    private fun validateSchoolAndRegionExists(school: String, region: String): Boolean {
        val schoolAndRegionMap =
            schoolsNameTask.result!!.getAll().zip(regionsNameTask.result!!.getAll()).toMap()

        return schoolAndRegionMap.containsKey(school) && schoolAndRegionMap[school] == region
    }

    override fun getArgs(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["student"] = setupStudent
        map["characteristicsMaleTask"] = characteristicsMaleTask
        map["characteristicsFemaleTask"] = characteristicsFemaleTask
        map["characteristicsWithCategoriesMaleTask"] = characteristicsWithCategoriesMaleTask
        map["characteristicsWithCategoriesFemaleTask"] = characteristicsWithCategoriesFemaleTask
        map["hobbiesResourceTask"] = hobbiesResourceTask
        map["hobbyCardTask"] = hobbyCardTask
        map["schoolsNameTask"] = schoolsNameTask
        map["regionsNameTask"] = regionsNameTask
        map["activity"] = this
        return map
    }

    fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> setupStudent.grade = Grade.TENTH
            R.id.gradeEleventh -> setupStudent.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> setupStudent.grade = Grade.TWELFTH
            else -> {
            }
        }
    }

    interface OnNextClickForCharacteristics {
        fun onNextClick(): Task<Boolean>
        fun onBackClick(): Task<Boolean>
    }

}

sealed class StudentResult

data class Success(val student: Student) : StudentResult()

data class Failure(val msg: String) : StudentResult()
