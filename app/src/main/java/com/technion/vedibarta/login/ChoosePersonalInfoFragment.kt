package com.technion.vedibarta.login


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.extensions.putGender
import com.technion.vedibarta.viewModels.UserSetupViewModel
import com.technion.vedibarta.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.utilities.resourcesManagement.TextResource
import kotlinx.android.synthetic.main.fragment_choose_personal_info.*


/**
 * A simple [Fragment] subclass.
 */
class ChoosePersonalInfoFragment : VedibartaFragment() {


    private val TAG = "PersonalInfoFragment"
    private val BORDER_WIDTH = 10

    // Tag -> (schoolName, SchoolRegion)
//    private lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>
    private lateinit var schoolAndRegionMap: Map<String, String>


    private lateinit var argumentTransfer: ArgumentTransfer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        argumentTransfer = context as? ArgumentTransfer
            ?: throw ClassCastException("$context must implement ${ArgumentTransfer::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_personal_info, container, false)
    }

    private fun onButtonFemaleClickListener() {

        imageFemale.borderWidth = BORDER_WIDTH
        imageFemale.borderColor = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
        imageMale.borderWidth = 0
        textOptionFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccentDark))
        textOptionMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.background))
        (activity as UserSetupActivity).setupStudent.gender = Gender.FEMALE
        PreferenceManager.getDefaultSharedPreferences(activity as UserSetupActivity).edit()
            .putGender(Gender.FEMALE).apply()
    }

    private fun onButtonMaleClickListener() {

        imageMale.borderWidth = BORDER_WIDTH
        imageMale.borderColor = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
        imageFemale.borderWidth = 0
        textOptionMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccentDark))
        textOptionFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.background))

        (activity as UserSetupActivity).setupStudent.gender = Gender.MALE
        PreferenceManager.getDefaultSharedPreferences(activity as UserSetupActivity).edit()
            .putGender(Gender.MALE).apply()
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
        schoolListSpinner.clearFocus()
        regionListSpinner.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int) {
        val schoolName = schoolListSpinner.adapter.getItem(position).toString()
        val region = schoolAndRegionMap[schoolName].toString()

        regionListSpinner.text = SpannableStringBuilder(region)
        (activity as UserSetupActivity).setupStudent.school = schoolName
        (activity as UserSetupActivity).setupStudent.region = region

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    private fun onRegionSelectedListener(position: Int) {

        schoolListSpinner.text = SpannableStringBuilder("")
        val region = regionListSpinner.adapter.getItem(position).toString()
        (activity as UserSetupActivity).setupStudent.region = region
        (activity as UserSetupActivity).setupStudent.school = ""

        val schoolList = schoolAndRegionMap.filter { it.value == region }.keys.toTypedArray()

        populateAutoTextView(activity as UserSetupActivity, schoolListSpinner, schoolList)

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAndInitViews(view)
    }

    override fun setupAndInitViews(v: View) {
        super.setupAndInitViews(v)
        val argMap = argumentTransfer.getArgs()
        genderInit()
        val act = argMap["activity"] as Activity
        val schoolsNameTask = argMap["schoolsNameTask"] as Task<TextResource>
        val regionsNameTask = argMap["regionsNameTask"] as Task<TextResource>
        Tasks.whenAll(schoolsNameTask, regionsNameTask)
            .addOnSuccessListener(act) {
                extraOptionsInit(v, schoolsNameTask, regionsNameTask)
            }
    }

    private fun genderInit() {
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_man).into(imageMale)
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_girl).into(imageFemale)
        imageMale.setOnClickListener { onButtonMaleClickListener() }
        imageFemale.setOnClickListener { onButtonFemaleClickListener() }

        when ((activity as UserSetupActivity).setupStudent.gender) {
            Gender.MALE -> {
                imageMale.borderWidth = BORDER_WIDTH
                imageMale.borderColor = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
                imageFemale.borderWidth = 0
                textOptionMale.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccentDark
                    )
                )
                textOptionFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.background))
            }
            Gender.FEMALE -> {
                imageFemale.borderWidth = BORDER_WIDTH
                imageFemale.borderColor = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
                imageMale.borderWidth = 0
                textOptionFemale.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccentDark
                    )
                )
                textOptionMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.background))
            }
            Gender.NONE -> {
            }
        }
    }

    private fun extraOptionsInit(
        v: View,
        schoolsNameTask: Task<TextResource>,
        regionsNameTask: Task<TextResource>
    ) {

        val act = (activity as UserSetupActivity)

        schoolAndRegionMap = schoolsNameTask.result!!.getAll().zip(regionsNameTask.result!!.getAll()).toMap()

        // ---Student Name Views---
        val firstName: TextInputEditText = v.findViewById(R.id.textFieldFirstName)
        val lastName: TextInputEditText = v.findViewById(R.id.textFieldLastName)

        textFieldFirstName.doOnTextChanged { text, _, _, _ ->
            if (text!!.matches(
                    resources.getString(R.string.allowed_letters_regex).toRegex()
                ) || text.isBlank()
            )
                act.chosenFirstName = text.toString()
            else {
                firstName.text = SpannableStringBuilder("")
            }
        }

        textFieldLastName.doOnTextChanged { text, _, _, _ ->
            if (text!!.matches(
                    resources.getString(R.string.allowed_letters_regex).toRegex()
                ) || text.isBlank()
            )
                act.chosenLastName = text.toString()
            else {
                lastName.text = SpannableStringBuilder("")
            }
        }

        schoolListSpinner.setOnItemClickListener { _, _, position, _ ->
            onSchoolSelectedListener(
                position
            )
        }
        regionListSpinner.setOnItemClickListener { _, _, position, _ ->
            onRegionSelectedListener(
                position
            )
        }
        schoolListSpinner.doOnTextChanged { text, _, _, _ ->
            (activity as UserSetupActivity).setupStudent.school = text.toString()
        }

        regionListSpinner.doOnTextChanged { text, _, _, _ ->
            populateAutoTextView(
                act,
                schoolListSpinner,
                schoolsNameTask.result!!.getAll().toTypedArray()
            )
            (activity as UserSetupActivity).setupStudent.region = text.toString()
        }

        //---Populate DropDownLists---
        populateAutoTextView(
            act,
            schoolListSpinner,
            schoolsNameTask.result!!.getAll().toTypedArray()
        )
        populateAutoTextView(
            act,
            regionListSpinner,
            regionsNameTask.result!!.getAll().distinct().toTypedArray()
        )


        textViewFirstNameTitle.markRequired()
        textViewLastNameTitle.markRequired()
        schoolFilterSwitchText.markRequired()
        regionFilterSwitchText.markRequired()
        gradeText.markRequired()

        personalInfoButton.setOnClickListener {
            if (cardViewNameBody.visibility == View.VISIBLE)
                cardViewNameBody.visibility = View.GONE
            else
                cardViewNameBody.visibility = View.VISIBLE
            nameArrowButton.switchState()
        }


        schoolInfoButton.setOnClickListener {
            if (cardViewSchoolBody.visibility == View.VISIBLE)
                cardViewSchoolBody.visibility = View.GONE
            else
                cardViewSchoolBody.visibility = View.VISIBLE
            schoolArrowButton.switchState()
        }
        schoolListSpinner.text = SpannableStringBuilder(act.setupStudent.school)
        regionListSpinner.text = SpannableStringBuilder(act.setupStudent.region)

        nameArrowButton.switchState()
        schoolArrowButton.switchState()

        nameArrowButton.setAnimationDuration(200)
        schoolArrowButton.setAnimationDuration(200)

        nameArrowButton.setOnClickListener {
            if (cardViewNameBody.visibility == View.VISIBLE)
                cardViewNameBody.visibility = View.GONE
            else
                cardViewNameBody.visibility = View.VISIBLE
            nameArrowButton.switchState()
        }

        schoolArrowButton.setOnClickListener {
            if (cardViewSchoolBody.visibility == View.VISIBLE)
                cardViewSchoolBody.visibility = View.GONE
            else
                cardViewSchoolBody.visibility = View.VISIBLE
            schoolArrowButton.switchState()
        }

    }
}
