package com.technion.vedibarta.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.fragments.HobbiesFragment
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.extensions.executeAfterTimeoutInMillis
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.Resource
import kotlinx.android.synthetic.main.activity_user_setup.*
import java.sql.Timestamp

class UserSetupActivity : VedibartaActivity(), VedibartaFragment.ArgumentTransfer, ChoosePersonalInfoFragment.OnGenderSelect {

    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    private var missingDetailsText = ""

    var setupStudent = Student(
        uid = userId!!,
        lastActivity = Timestamp(System.currentTimeMillis())
    )

    lateinit var hobbyCardTask: Task<List<HobbyCard>>
    lateinit var hobbiesResourceTask: Task<MultilingualResource>

    lateinit var characteristicsMaleTask : Task<MultilingualResource>
    lateinit var characteristicsFemaleTask: Task<MultilingualResource>

    lateinit var schoolsNameTask: Task<out Resource>
    lateinit var regionsNameTask: Task<out Resource>
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

        hobbiesResourceTask = RemoteResourcesManager(this)
            .findMultilingualResource("hobbies/all")

        hobbyCardTask = VedibartaFragment.loadHobbies(this)

        characteristicsMaleTask = RemoteResourcesManager(this).findMultilingualResource("characteristics", Gender.MALE)
        characteristicsFemaleTask = RemoteResourcesManager(this).findMultilingualResource("characteristics",Gender.FEMALE)

        schoolsNameTask = RemoteResourcesManager(this).findResource("schools")
        regionsNameTask = RemoteResourcesManager(this).findResource("regions")
        schoolTags = resources.getIntArray(R.array.schoolTagList).toTypedArray()
        Tasks.whenAll(schoolsNameTask,regionsNameTask)
            .executeAfterTimeoutInMillis(5000L){
                internetConnectionErrorHandler(this)
            }
            .addOnSuccessListener(this){
                loading.visibility = View.GONE
                layout.visibility = View.VISIBLE
            }
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
        adapter.addFragment(ChoosePersonalInfoFragment(), "1")
        adapter.addFragment(ChooseCharacteristicsFragment(), "2")
        adapter.addFragment(HobbiesFragment(), "3")

        viewPager.setPagingEnabled(false)
        viewPager.setOnInterceptTouchEventCustomBehavior {
            if (setupStudent.gender != Gender.NONE) {
                viewPager.setPagingEnabled(true)
                viewPager.setOnInterceptTouchEventCustomBehavior { }
            }
        }

        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
    }

    override fun onBackPressed() {

    }

    private fun onDoneClick(){
        validateUserInput()
            .addOnSuccessListener(this){
                if (it) {
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

    private fun validateUserInput(): Task<Boolean> {
        return Tasks.whenAll(characteristicsFemaleTask, characteristicsMaleTask, schoolsNameTask, regionsNameTask, hobbiesResourceTask, hobbyCardTask)
            .continueWith {
                missingDetailsText = ""
                val studentsCharacteristics = setupStudent.characteristics.filter { it.value }.keys

                if (setupStudent.gender == Gender.NONE) {
                    missingDetailsText += "${resources.getString(R.string.user_setup_gender_missing)}\n"
                    return@continueWith false
                }

                Log.d(TAG, "first: $chosenFirstName last: $chosenLastName")

                if (chosenFirstName == "") {
                    missingDetailsText += "${resources.getString(R.string.user_setup_first_name_missing)}\n"
                    return@continueWith false
                }

                if (chosenLastName == "") {
                    missingDetailsText += "${resources.getString(R.string.user_setup_last_name_missing)}\n"
                    return@continueWith false
                }

                if (!schoolsNameTask.result!!.getAll().contains(
                        setupStudent.school
                    )
                ) {
                    missingDetailsText += "${resources.getString(R.string.user_setup_school_missing)}\n"
                    return@continueWith false
                }

                if (!regionsNameTask.result!!.getAll().contains(
                        setupStudent.region
                    )
                ) {
                    missingDetailsText += "${resources.getString(R.string.user_setup_region_missing)}\n"
                    return@continueWith false
                }

                if (!validateSchoolAndRegionExists()) {
                    missingDetailsText += "${resources.getString(R.string.user_setup_wrong_school_and_region_combination)}\n"
                    return@continueWith false
                }

                if (studentsCharacteristics.isEmpty()) {
                    missingDetailsText += "${resources.getString(R.string.user_setup_characteristics_missing)}\n"
                    return@continueWith false
                }

                if (setupStudent.hobbies.isEmpty()) {
                    missingDetailsText += "${resources.getString(R.string.user_setup_hobbies_missing)}\n"
                    return@continueWith false
                }

                true
            }
    }

    private fun validateSchoolAndRegionExists() : Boolean{
        val schoolAndRegionMap =
            schoolTags.zip(schoolsNameTask.result!!.getAll().zip(regionsNameTask.result!!.getAll().distinct()))
                .toMap()
        var result = false
        schoolsNameTask.result!!.getAll().forEachIndexed { index, name ->
            if (name == setupStudent.school){
                result = (schoolAndRegionMap[schoolTags[index]] ?: error("")).second == setupStudent.region

                if (result)
                    return result
            }
        }
        return result
    }

    override fun getArgs(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["student"] = setupStudent
        map["characteristicsMaleTask"] = characteristicsMaleTask
        map["characteristicsFemaleTask"] = characteristicsFemaleTask
        map["hobbiesResourceTask"] = hobbiesResourceTask
        map["hobbyCardTask"] = hobbyCardTask
        map["schoolsNameTask"] = schoolsNameTask
        map["regionsNameTask"] = regionsNameTask
        map["activity"] = this
        return map
    }

    override fun reloadCharacteristics() {
        (userSetupContainer.adapter as SectionsPageAdapter)
            .notifyDataSetChanged()
    }
}
