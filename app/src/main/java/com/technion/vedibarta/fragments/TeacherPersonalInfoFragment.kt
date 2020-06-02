package com.technion.vedibarta.fragments

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.POJOs.Unfilled
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.SchoolsAdapter
import com.technion.vedibarta.data.viewModels.SchoolInfo
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import com.technion.vedibarta.data.viewModels.teacherSetupViewModelFactory
import com.technion.vedibarta.teacher.Failure
import com.technion.vedibarta.teacher.Success
import com.technion.vedibarta.teacher.TeacherResult
import com.technion.vedibarta.utilities.extensions.putGender
import com.technion.vedibarta.utilities.missingDetailsDialog
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.*

private const val SCHOOL_NAME = "schoolsName"
private const val REGION_NAME = "regionsName"
private const val BORDER_WIDTH = 10

class TeacherPersonalInfoFragment(

) : Fragment() {

    val viewModel: TeacherSetupViewModel by activityViewModels() {
        teacherSetupViewModelFactory(requireActivity().applicationContext)
    }

    private lateinit var schoolAndRegionMap: Map<String, String>
    private lateinit var schoolsName: Array<String>
    private lateinit var regionsName: Array<String>
    private lateinit var schoolListItemLongCLick: SchoolListItemLongCLick

    companion object {
        @JvmStatic
        fun newInstance(schoolsName: Array<String>, regionsName: Array<String>) =
            TeacherPersonalInfoFragment().apply {
                arguments = Bundle().apply {
                    this.putStringArray(SCHOOL_NAME, schoolsName)
                    this.putStringArray(REGION_NAME, regionsName)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        schoolListItemLongCLick = context as SchoolListItemLongCLick
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            schoolsName = it.getStringArray(SCHOOL_NAME)!!
            regionsName = it.getStringArray(REGION_NAME)!!
            schoolAndRegionMap = schoolsName.zip(regionsName).toMap()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_personal_info, container, false)

        view.findViewById<NestedScrollView>(R.id.teacherPersonalInfoScrollView).isNestedScrollingEnabled =
            false
        val schoolList = view.findViewById<RecyclerView>(R.id.schoolList)
        schoolList.isNestedScrollingEnabled = false
        schoolList.layoutManager = LinearLayoutManager(activity)
        val adapter = SchoolsAdapter(
            { onAddSchoolButtonClick() },
            { v: View -> schoolListItemLongCLick.onLongClickListener(v) },
            viewModel.schoolsList
        )
        schoolList.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val allowedLetters = getString(R.string.allowed_letters_regex)
        textFieldFirstName.doOnTextChanged { text, _, _, _ ->
            if (allowedLetters.toRegex().matches(text.toString())) {
                viewModel.chosenFirstName = Filled(text.toString())
                textFieldFirstName.error = null
            } else {
                textFieldFirstName.error = getString(R.string.text_field_error)
            }
        }
        textFieldLastName.doOnTextChanged { text, _, _, _ ->
            if (allowedLetters.toRegex().matches(text.toString())) {
                viewModel.chosenLastName = Filled(text.toString())
                textFieldFirstName.error = null
            } else {
                textFieldFirstName.error = getString(R.string.text_field_error)
            }
        }
        genderInit()
    }


    private fun onAddSchoolButtonClick() {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .positiveButton(R.string.add) {
                validateSchoolInfo().let { result ->
                    when (result) {
                        is Success -> {
                            viewModel.schoolsList.add(result.data)
                            viewModel.chosenGradesPerSchool.clear()
                            viewModel.chosenSchoolRegionPerSchool = Unfilled
                            viewModel.chosenSchoolNamePerSchool = Unfilled
                            schoolList.adapter!!.notifyItemInserted(viewModel.schoolsList.size)
                            it.dismiss()
                        }
                        is Failure -> {
                            missingDetailsDialog(requireContext(), result.msg)
                        }
                    }
                }
            }
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.fragment_add_school_dialog)
            .show {
                initMaterialDialog(this)

            }
    }

    private fun validateSchoolInfo(): TeacherResult {
        val school = when (val chosenSchool = viewModel.chosenSchoolNamePerSchool) {
            is Unfilled -> return Failure(
                resources.getString(R.string.user_setup_school_missing)
            )
            is Filled -> chosenSchool.text
        }

        if (!schoolsName.contains(school))
            return Failure(resources.getString(R.string.user_setup_school_missing))

        val region = when (val chosenRegion = viewModel.chosenSchoolRegionPerSchool) {
            is Unfilled -> return Failure(
                resources.getString(R.string.user_setup_region_missing)
            )
            is Filled -> chosenRegion.text
        }

        if (!regionsName.contains(region))
            return Failure(resources.getString(R.string.user_setup_region_missing))

        if (!(schoolAndRegionMap.containsKey(school) && schoolAndRegionMap[school] == region)) {
            return Failure(resources.getString(R.string.user_setup_wrong_school_and_region_combination))
        }

        if (viewModel.chosenGradesPerSchool.isEmpty()) {
            return Failure(resources.getString(R.string.teacher_setup_grade_missing))
        }
        val schoolInfo = SchoolInfo(school, region, viewModel.chosenGradesPerSchool.toList())
        if (viewModel.schoolsList.contains(schoolInfo))
            return Failure(resources.getString(R.string.teacher_setup_school_already_exist))

        return Success(schoolInfo)
    }



    private fun initMaterialDialog(materialDialog: MaterialDialog) {
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTenth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    viewModel.chosenGradesPerSchool.add(Grade.TENTH)
                else
                    viewModel.chosenGradesPerSchool.remove(Grade.TENTH)
            }
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeEleventh)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    viewModel.chosenGradesPerSchool.add(Grade.ELEVENTH)
                else
                    viewModel.chosenGradesPerSchool.remove(Grade.ELEVENTH)
            }
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTwelfth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    viewModel.chosenGradesPerSchool.add(Grade.TWELFTH)
                else
                    viewModel.chosenGradesPerSchool.remove(Grade.TWELFTH)
            }
        val schoolSpinner =
            materialDialog.findViewById<AutoCompleteTextView>(R.id.schoolListSpinner)
        val regionSpinner =
            materialDialog.findViewById<AutoCompleteTextView>(R.id.regionListSpinner)

        schoolSpinner.setAdapter(
            ArrayAdapter(
                requireContext().applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                schoolsName
            )
        )

        regionSpinner.setAdapter(
            ArrayAdapter(
                requireContext().applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                regionsName
            )
        )

        schoolSpinner.setOnItemClickListener { _, view, _, _ ->
            onSchoolSelectedListener(materialDialog, view)
        }

        regionSpinner.setOnItemClickListener { _, view, _, _ ->
            onRegionSelectedListener(materialDialog, view)
        }

        schoolSpinner.doOnTextChanged { text, _, _, _ ->
            viewModel.chosenSchoolNamePerSchool = Filled(text.toString())
        }

        regionSpinner.doOnTextChanged { text, _, _, _ ->
            schoolSpinner.setAdapter(
                ArrayAdapter(
                    requireContext().applicationContext,
                    android.R.layout.simple_dropdown_item_1line,
                    schoolsName
                )
            )
            viewModel.chosenSchoolRegionPerSchool = Filled(text.toString())
        }
    }

    private fun onSchoolSelectedListener(
        md: MaterialDialog,
        v: View
    ) {
        val regionSpinner = md.findViewById<AutoCompleteTextView>(R.id.regionListSpinner)
        val schoolName = (v as TextView).text.toString()

        val region = schoolAndRegionMap[schoolName].toString()
        viewModel.chosenSchoolNamePerSchool = Filled(schoolName)
        viewModel.chosenSchoolRegionPerSchool = Filled(region)
        regionSpinner.text = SpannableStringBuilder(region)
    }

    private fun onRegionSelectedListener(
        md: MaterialDialog,
        v: View
    ) {
        val schoolSpinner = md.findViewById<AutoCompleteTextView>(R.id.schoolListSpinner)
        schoolSpinner.text = SpannableStringBuilder("")
        val region = (v as TextView).text.toString()
        val schoolList = schoolAndRegionMap.filter { it.value == region }.keys.toTypedArray()
        viewModel.chosenSchoolNamePerSchool = Unfilled
        viewModel.chosenSchoolRegionPerSchool = Filled(region)
        schoolSpinner.setAdapter(
            ArrayAdapter(
                requireContext().applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                schoolList
            )
        )
    }

    private fun onButtonFemaleClickListener() {
        imageFemale.borderWidth = BORDER_WIDTH
        imageFemale.borderColor = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
        imageMale.borderWidth = 0
        textOptionFemale.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorAccentDark
            )
        )
        textOptionMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))

        viewModel.gender.value = Gender.FEMALE

        PreferenceManager.getDefaultSharedPreferences(activity).edit {
            putGender(Gender.FEMALE)
        }
    }

    private fun onButtonMaleClickListener() {
        imageMale.borderWidth = BORDER_WIDTH
        imageMale.borderColor = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
        imageFemale.borderWidth = 0
        textOptionMale.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorAccentDark
            )
        )
        textOptionFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))

        viewModel.gender.value = Gender.MALE

        PreferenceManager.getDefaultSharedPreferences(activity).edit {
            putGender(Gender.MALE)
        }
    }

    private fun genderInit() {
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_man).into(imageMale)
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_girl)
            .into(imageFemale)
        imageMale.setOnClickListener { onButtonMaleClickListener() }
        imageFemale.setOnClickListener { onButtonFemaleClickListener() }

        when (viewModel.gender.value) {
            Gender.MALE -> {
                imageMale.borderWidth =
                    BORDER_WIDTH
                imageMale.borderColor =
                    ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
                imageFemale.borderWidth = 0
                textOptionMale.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccentDark
                    )
                )
                textOptionFemale.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.textPrimary
                    )
                )
            }
            Gender.FEMALE -> {
                imageFemale.borderWidth =
                    BORDER_WIDTH
                imageFemale.borderColor =
                    ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
                imageMale.borderWidth = 0
                textOptionFemale.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccentDark
                    )
                )
                textOptionMale.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.textPrimary
                    )
                )
            }
            Gender.NONE -> {
            }
        }
    }

}

interface SchoolListItemLongCLick {
    fun onLongClickListener(v: View): Boolean
}


