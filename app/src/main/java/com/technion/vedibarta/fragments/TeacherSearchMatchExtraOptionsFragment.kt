package com.technion.vedibarta.fragments

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.R
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.data.viewModels.TeacherSearchMatchViewModel
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.extensions.parentViewModels
import kotlinx.android.synthetic.main.fragment_teacher_search_match_extra_options.*

class TeacherSearchMatchExtraOptionsFragment : Fragment() {

    private val viewModel: TeacherSearchMatchViewModel by parentViewModels()

    private lateinit var schoolAndRegionMap: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_search_match_extra_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAndInitViews()
    }

    private fun setupAndInitViews() {
        val schools = TeacherResources.schools
        val regions = TeacherResources.regions

        schoolAndRegionMap = schools.getAll().zip(regions.getAll()).toMap()

        //---Switch Views---
        schoolFilterSwitch.setOnCheckedChangeListener { _, isChecked -> schoolOnCheckedChanged(isChecked) }
        regionFilterSwitch.setOnCheckedChangeListener { _, isChecked -> regionOnCheckedChanged(isChecked) }
        scheduleFilterSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.searchBySchedule = isChecked }

        //---DropDownList Views---
        schoolListSpinner.setOnItemClickListener { _, _, pos, _ -> onSchoolSelectedListener(pos) }
        regionListSpinner.setOnItemClickListener { _, _, pos, _ -> onRegionSelectedListener(pos) }

        regionListSpinner.doOnTextChanged { text, _, _, _ ->
            VedibartaFragment.populateAutoTextView(
                requireContext(),
                schoolListSpinner,
                schools.getAll().toTypedArray()
            )
            viewModel.region = if (text.isNullOrEmpty()) null else text.toString()
        }

        schoolListSpinner.doOnTextChanged { text, _, _, _ ->
            viewModel.school = if (text.isNullOrEmpty()) null else text.toString()
        }

        gradeFilterSwitch.setOnCheckedChangeListener { _, isChecked ->  gradeOnCheckChanged(isChecked)}

        VedibartaFragment.populateAutoTextView(
            requireContext(),
            regionListSpinner,
            regions.getAll().distinct().toTypedArray()
        )
        VedibartaFragment.populateAutoTextView(
            requireContext(),
            schoolListSpinner,
            schools.getAll().toTypedArray()
        )

        gradeTenth.setOnClickListener { onRadioButtonClicked(it) }
        gradeEleventh.setOnClickListener { onRadioButtonClicked(it) }
        gradeTwelfth.setOnClickListener { onRadioButtonClicked(it) }
    }

    private fun schoolOnCheckedChanged(isChecked: Boolean) {
        VedibartaActivity.hideKeyboard(requireActivity())
        if (isChecked) {
            schoolListSpinner.visibility = View.VISIBLE
        } else {
            schoolListSpinner.text.clear()
            viewModel.school = null
            schoolListSpinner.visibility = View.GONE
        }
    }

    private fun regionOnCheckedChanged(isChecked: Boolean) {
        VedibartaActivity.hideKeyboard(requireActivity())
        if (isChecked) {
            regionListSpinner.visibility = View.VISIBLE
        } else {
            regionListSpinner.text.clear()
            viewModel.region = null
            regionListSpinner.visibility = View.GONE
        }
    }

    private fun onSchoolSelectedListener(position: Int) {
        val schoolName = schoolListSpinner.adapter.getItem(position).toString()
        val region = schoolAndRegionMap[schoolName].toString()

        regionListSpinner.text = SpannableStringBuilder(region)
        viewModel.school = schoolName
        viewModel.region = region

        VedibartaActivity.hideKeyboard(requireActivity())
    }

    private fun onRegionSelectedListener(position: Int) {
        schoolListSpinner.text = SpannableStringBuilder("")
        val region = regionListSpinner.adapter.getItem(position).toString()
        viewModel.region = region
        viewModel.school = null

        val schoolList = schoolAndRegionMap.filter { it.value == region }.keys.toTypedArray()

        VedibartaFragment.populateAutoTextView(requireContext(), schoolListSpinner, schoolList)

        VedibartaActivity.hideKeyboard(requireActivity())
    }

    private fun gradeOnCheckChanged(isChecked: Boolean){
        gradeRadioGroup.clearCheck()
        viewModel.grade = null
        if (isChecked)
            gradeRadioGroup.visibility = View.VISIBLE
        else
            gradeRadioGroup.visibility = View.GONE
    }

    private fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> viewModel.grade = Grade.TENTH
            R.id.gradeEleventh -> viewModel.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> viewModel.grade = Grade.TWELFTH
        }
    }
}