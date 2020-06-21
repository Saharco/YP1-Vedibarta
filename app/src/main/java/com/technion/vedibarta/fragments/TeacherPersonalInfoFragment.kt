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
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.SchoolsAdapter
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel.*
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.*
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.imageFemale
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.imageMale
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.textFieldFirstName
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.textFieldLastName
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.textOptionFemale
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.textOptionMale

private const val BORDER_WIDTH = 10

class TeacherPersonalInfoFragment : Fragment() {

    val viewModel: TeacherSetupViewModel by activityViewModels()
    private lateinit var schoolListItemLongCLick: SchoolListItemLongCLick

    override fun onAttach(context: Context) {
        super.onAttach(context)
        schoolListItemLongCLick = context as SchoolListItemLongCLick
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_personal_info, container, false)

        view.findViewById<NestedScrollView>(R.id.teacherPersonalInfoScrollView).isNestedScrollingEnabled = false

        val schoolList = view.findViewById<RecyclerView>(R.id.schoolList)
        schoolList.isNestedScrollingEnabled = false
        schoolList.layoutManager = LinearLayoutManager(activity)
        val adapter = SchoolsAdapter(
            { v: View -> schoolListItemLongCLick.onLongClickListener(v) },
            viewModel.schoolsInfo
        )
        schoolList.adapter = adapter

        val addSchoolButton = view.findViewById<TextView>(R.id.addSchoolButton)
        addSchoolButton.setOnClickListener {
            onAddSchoolButtonClick()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val allowedLetters = getString(R.string.allowed_letters_regex)
        textFieldFirstName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                viewModel.firstName = null
            } else {
                if (allowedLetters.toRegex().matches(text.toString())) {
                    viewModel.firstName = text.toString()
                    textFieldFirstName.error = null
                } else {
                    textFieldFirstName.error = getString(R.string.text_field_error)
                }
            }
        }

        textFieldLastName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                viewModel.lastName = null
            } else {
                if (allowedLetters.toRegex().matches(text.toString())) {
                    viewModel.lastName = text.toString()
                    textFieldFirstName.error = null
                } else {
                    textFieldFirstName.error = getString(R.string.text_field_error)
                }
            }
        }

        genderInit()
    }

    private fun onAddSchoolButtonClick() {
        val viewModel = viewModel.AddSchoolDialogViewModel()

        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .positiveButton(R.string.add) {
                viewModel.addPressed().also { result ->
                    if (result is AddResult.Success) {
                        schoolList.adapter!!.notifyItemInserted(result.insertPos)
                        it.dismiss()
                    }
                }
            }
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.fragment_add_school_dialog)
            .show {
                initMaterialDialog(this, viewModel)
            }
    }

    private fun initMaterialDialog(
        materialDialog: MaterialDialog,
        viewModel: AddSchoolDialogViewModel
    ) {
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTenth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    viewModel.grades.add(Grade.TENTH)
                else
                    viewModel.grades.remove(Grade.TENTH)
            }

        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeEleventh)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    viewModel.grades.add(Grade.ELEVENTH)
                else
                    viewModel.grades.remove(Grade.ELEVENTH)
            }

        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTwelfth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    viewModel.grades.add(Grade.TWELFTH)
                else
                    viewModel.grades.remove(Grade.TWELFTH)
            }

        val schoolSpinner = materialDialog.findViewById<AutoCompleteTextView>(R.id.schoolListSpinner)
        val regionSpinner = materialDialog.findViewById<AutoCompleteTextView>(R.id.regionListSpinner)

        schoolSpinner.setAdapter(
            ArrayAdapter(
                requireContext().applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                viewModel.allSchools
            )
        )

        regionSpinner.setAdapter(
            ArrayAdapter(
                requireContext().applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                viewModel.allRegions
            )
        )

        schoolSpinner.setOnItemClickListener { _, view, _, _ ->
            onSchoolSelectedListener(materialDialog, view as TextView, viewModel)
        }

        regionSpinner.setOnItemClickListener { _, view, _, _ ->
            onRegionSelectedListener(materialDialog, view as TextView, viewModel)
        }

        schoolSpinner.doOnTextChanged { text, _, _, _ ->
            viewModel.school = text.toString()
        }

        regionSpinner.doOnTextChanged { text, _, _, _ ->
            schoolSpinner.setAdapter(
                ArrayAdapter(
                    requireContext().applicationContext,
                    android.R.layout.simple_dropdown_item_1line,
                    viewModel.schoolAndRegionMap.filter { it.value == text.toString() }.keys.toTypedArray()
                )
            )
            viewModel.region = text.toString()
        }
    }

    private fun onSchoolSelectedListener(
        md: MaterialDialog,
        v: TextView,
        viewModel: AddSchoolDialogViewModel
    ) {
        val regionSpinner = md.findViewById<AutoCompleteTextView>(R.id.regionListSpinner)
        val schoolName = v.text.toString()

        val region = viewModel.schoolAndRegionMap[schoolName].toString()
        viewModel.school = schoolName
        viewModel.region = region
        regionSpinner.text = SpannableStringBuilder(region)
    }

    private fun onRegionSelectedListener(
        md: MaterialDialog,
        v: TextView,
        viewModel: AddSchoolDialogViewModel
    ) {
        val schoolSpinner = md.findViewById<AutoCompleteTextView>(R.id.schoolListSpinner)
        schoolSpinner.text = SpannableStringBuilder("")
        val region = v.text.toString()
        val schoolList = viewModel.schoolAndRegionMap.filter { it.value == region }.keys.toTypedArray()
        viewModel.school = null
        viewModel.region = region
        schoolSpinner.setAdapter(
            ArrayAdapter(
                requireContext().applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                schoolList
            )
        )
    }

    private fun genderInit() {
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_man).into(imageMale)
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_girl).into(imageFemale)
        imageMale.setOnClickListener { viewModel.gender.value = Gender.MALE }
        imageFemale.setOnClickListener { viewModel.gender.value = Gender.FEMALE }

        viewModel.gender.observe(viewLifecycleOwner) {
            when (it!!) {
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
                    textOptionFemale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.textPrimaryOnDarkSurface
                        )
                    )
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
                    textOptionMale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.textPrimaryOnDarkSurface
                        )
                    )
                }
                Gender.NONE -> {
                    textOptionMale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.textPrimaryOnDarkSurface
                        )
                    )
                    textOptionFemale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.textPrimaryOnDarkSurface
                        )
                    )
                }
            }
        }
    }
}

interface SchoolListItemLongCLick {
    fun onLongClickListener(v: View): Boolean
}


