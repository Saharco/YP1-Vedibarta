package com.technion.vedibarta.fragments

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
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
import com.technion.vedibarta.data.viewModels.*
import com.technion.vedibarta.utilities.extensions.putGender
import com.technion.vedibarta.utilities.missingDetailsDialog
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.*
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.*

private const val SCHOOL_NAME = "schoolsName"
private const val REGION_NAME = "regionsName"
private const val BORDER_WIDTH = 10

class TeacherPersonalInfoFragment : Fragment() {

    val viewModel: TeacherSetupViewModel by activityViewModels {
        teacherSetupViewModelFactory(requireActivity().applicationContext)
    }

    private lateinit var schoolPressHandlers: SchoolPressHandlers
    private lateinit var schoolsName: Array<String>
    private lateinit var regionsName: Array<String>

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
        schoolPressHandlers = context as SchoolPressHandlers
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            schoolsName = it.getStringArray(SCHOOL_NAME)!!
            regionsName = it.getStringArray(REGION_NAME)!!
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_personal_info, container, false)
        setHasOptionsMenu(true)
        view.findViewById<NestedScrollView>(R.id.teacherPersonalInfoScrollView).isNestedScrollingEnabled =
            false
        val schoolList = view.findViewById<RecyclerView>(R.id.schoolList)
        schoolList.isNestedScrollingEnabled = false
        schoolList.layoutManager = LinearLayoutManager(activity)
        val adapter = SchoolsAdapter(
            { openSchoolDialog() },
            { v: View -> schoolPressHandlers.onLongPress(v) },
            { v: View -> schoolPressHandlers.onClick(v) },
            viewModel.schoolsList
        )
        schoolList.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val allowedLetters = getString(R.string.allowed_letters_regex)
        val appCompat = requireActivity() as AppCompatActivity
        appCompat.setSupportActionBar(toolbar)
        viewModel.event.observe(viewLifecycleOwner) { event ->
            if (!event.handled) {
                when (event) {
                    is TeacherSetupViewModel.Event.SchoolAdded -> schoolList.adapter?.notifyItemInserted(
                        viewModel.schoolsList.size
                    )
                    is TeacherSetupViewModel.Event.SchoolRemoved -> schoolList.adapter?.notifyItemRemoved(
                        event.index
                    )
                    is TeacherSetupViewModel.Event.SchoolEdited -> schoolList.adapter?.notifyItemChanged(
                        event.index
                    )
                    is TeacherSetupViewModel.Event.OpenSchoolDialog -> openSchoolDialog(event.isEdit)
                    else -> return@observe
                }
                event.handled = true
            }

        }
        textFieldFirstName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                viewModel.chosenFirstName = Unfilled
            } else {
                if (allowedLetters.toRegex().matches(text.toString())) {
                    viewModel.chosenFirstName = Filled(text.toString())
                    textFieldFirstName.error = null
                } else {
                    textFieldFirstName.error = getString(R.string.text_field_error)
                }
            }

        }
        textFieldLastName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                viewModel.chosenFirstName = Unfilled
            } else {
                if (allowedLetters.toRegex().matches(text.toString())) {
                    viewModel.chosenLastName = Filled(text.toString())
                    textFieldFirstName.error = null
                } else {
                    textFieldFirstName.error = getString(R.string.text_field_error)
                }
            }
        }
        genderInit()
    }

    private fun openSchoolDialog(isEdit: Boolean = false) {
        val positiveString = if (isEdit) R.string.save else R.string.add
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .positiveButton(positiveString) {
                val schoolViewModel: SchoolViewModel by viewModels {
                    schoolViewModelFactory(schoolsName, regionsName)
                }
                if (isEdit)
                    schoolViewModel.editSchool(viewModel.schoolsList, viewModel.selectedItemsList.first().tag.toString().toInt())
                else
                    schoolViewModel.createSchool(viewModel.schoolsList)
            }
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.fragment_add_school_dialog)
            .show {
                initMaterialDialog(this, isEdit)
            }
    }


    private fun initMaterialDialog(materialDialog: MaterialDialog, isEdit: Boolean) {
        val schoolViewModel: SchoolViewModel by viewModels {
            schoolViewModelFactory(schoolsName, regionsName)
        }
        schoolViewModel.event.removeObservers(viewLifecycleOwner)
        schoolViewModel.event.observe(viewLifecycleOwner) { event ->
            if (!event.handled) {
                when (event) {
                    is SchoolViewModel.SchoolEvent.SchoolAdded -> {
                        viewModel.addSchool(event.school)
                        materialDialog.dismiss()
                    }
                    is SchoolViewModel.SchoolEvent.SchoolEdited -> {
                        viewModel.editSchool(
                            event.school,
                            viewModel.selectedItemsList.first().tag.toString().toInt()
                        )
                        materialDialog.dismiss()
                    }
                    is SchoolViewModel.SchoolEvent.DisplayMissingInfoDialog -> missingDetailsDialog(
                        requireContext(),
                        getString(event.msgResId)
                    )
                    is SchoolViewModel.SchoolEvent.DisplayError -> Toast.makeText(
                        requireContext(),
                        event.msgResId,
                        Toast.LENGTH_LONG
                    ).show()
                }
                event.handled = true
            }
        }

        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTenth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    schoolViewModel.chosenGradesPerSchool.add(Grade.TENTH)
                else
                    schoolViewModel.chosenGradesPerSchool.remove(Grade.TENTH)
            }
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeEleventh)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    schoolViewModel.chosenGradesPerSchool.add(Grade.ELEVENTH)
                else
                    schoolViewModel.chosenGradesPerSchool.remove(Grade.ELEVENTH)
            }
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTwelfth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    schoolViewModel.chosenGradesPerSchool.add(Grade.TWELFTH)
                else
                    schoolViewModel.chosenGradesPerSchool.remove(Grade.TWELFTH)
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
            schoolViewModel.onSchoolSelectedListener(materialDialog, view)
        }

        regionSpinner.setOnItemClickListener { _, view, _, _ ->
            schoolViewModel.onRegionSelectedListener(materialDialog, view)
        }


        schoolSpinner.doOnTextChanged { text, _, _, _ ->
            schoolViewModel.chosenSchoolNamePerSchool = Filled(text.toString())
        }

        regionSpinner.doOnTextChanged { text, _, _, _ ->
            schoolSpinner.setAdapter(
                ArrayAdapter(
                    requireContext().applicationContext,
                    android.R.layout.simple_dropdown_item_1line,
                    schoolsName
                )
            )
            schoolViewModel.chosenSchoolRegionPerSchool = Filled(text.toString())
        }

        if (isEdit) {
            val schoolInfo =
                viewModel.schoolsList[viewModel.selectedItemsList.first().tag.toString()
                    .toInt()]
            schoolSpinner.text = SpannableStringBuilder(schoolInfo.schoolName)
            schoolViewModel.chosenSchoolNamePerSchool = Filled(schoolSpinner.text.toString())
            regionSpinner.text = SpannableStringBuilder(schoolInfo.schoolRegion)
            schoolViewModel.chosenSchoolRegionPerSchool = Filled(regionSpinner.text.toString())

            schoolInfo.grades.forEach {
                when (it) {
                    Grade.TENTH -> materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTenth).isChecked =
                        true
                    Grade.ELEVENTH -> materialDialog.findViewById<MaterialCheckBox>(R.id.gradeEleventh).isChecked =
                        true
                    Grade.TWELFTH -> materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTwelfth).isChecked =
                        true
                    else -> {
                    }
                }
            }
        }
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

    interface SchoolPressHandlers {
        fun onLongPress(v: View): Boolean
        fun onClick(v: View): Boolean
    }

}



