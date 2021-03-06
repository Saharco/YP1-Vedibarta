package com.technion.vedibarta.chatSearch

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_search_extra_options.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class SearchExtraOptionsFragment : VedibartaFragment() {

    private val TAG = "ExtraFragment@Search"

    private val viewModel: ChatSearchViewModel by activityViewModels()

    private lateinit var schoolAndRegionMap: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_extra_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAndInitViews()
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as ChatSearchActivity)
        regionListSpinner.clearFocus()
        schoolListSpinner.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int) {
        val schoolName = schoolListSpinner.adapter.getItem(position).toString()
        val region = schoolAndRegionMap[schoolName].toString()

        regionListSpinner.text = SpannableStringBuilder(region)
        viewModel.chosenSchool = Filled(schoolName)
        viewModel.chosenRegion = Filled(region)

        VedibartaActivity.hideKeyboard(requireActivity())
    }

    private fun onRegionSelectedListener(position: Int) {
        schoolListSpinner.text = SpannableStringBuilder("")
        val region = regionListSpinner.adapter.getItem(position).toString()
        viewModel.chosenRegion = Filled(region)
        viewModel.chosenSchool = Filled("")

        val schoolList = schoolAndRegionMap.filter { it.value == region }.keys.toTypedArray()

        populateAutoTextView(requireContext(), schoolListSpinner, schoolList)

        VedibartaActivity.hideKeyboard(requireActivity())
    }

    private fun schoolOnCheckedChanged(isChecked: Boolean) {
        schoolListSpinner.text.clear()
        viewModel.chosenSchool = Unfilled
        VedibartaActivity.hideKeyboard(requireActivity())
        if (isChecked) {
            schoolListSpinner.visibility = View.VISIBLE
        } else {
            schoolListSpinner.visibility = View.GONE
        }
    }

    private fun regionOnCheckedChanged(isChecked: Boolean) {
        VedibartaActivity.hideKeyboard(requireActivity())
        regionListSpinner.text.clear()
        viewModel.chosenRegion = Unfilled
        if (isChecked) {
            regionListSpinner.visibility = View.VISIBLE
        } else {
            regionListSpinner.visibility = View.GONE
        }
    }

    private fun gradeOnCheckChanged(isChecked: Boolean){
        gradeRadioGroup.clearCheck()
        viewModel.grade = Grade.NONE
        if (isChecked)
            gradeRadioGroup.visibility = View.VISIBLE
        else
            gradeRadioGroup.visibility = View.GONE
    }

    private fun setupAndInitViews() {
        val schools = StudentResources.schools
        val regions = StudentResources.regions

        schoolAndRegionMap = schools.getAll().zip(regions.getAll()).toMap()

        //---Switch Views---
        schoolFilterSwitch.setOnCheckedChangeListener { _, isChecked -> schoolOnCheckedChanged(isChecked) }
        regionFilterSwitch.setOnCheckedChangeListener { _, isChecked -> regionOnCheckedChanged(isChecked) }

        //---DropDownList Views---
        schoolListSpinner.setOnItemClickListener { _, _, pos, _ -> onSchoolSelectedListener(pos) }
        regionListSpinner.setOnItemClickListener { _, _, pos, _ -> onRegionSelectedListener(pos) }

        regionListSpinner.doOnTextChanged { text, _, _, _ ->
            populateAutoTextView(
                requireContext(),
                schoolListSpinner,
                schools.getAll().toTypedArray()
            )
            viewModel.chosenRegion = if (text.isNullOrEmpty()) Unfilled else Filled(text.toString())
        }

        schoolListSpinner.doOnTextChanged { text, _, _, _ ->
            viewModel.chosenSchool = if (text.isNullOrEmpty()) Unfilled else Filled(text.toString())
        }

        gradeFilterSwitch.setOnCheckedChangeListener { _, isChecked ->  gradeOnCheckChanged(isChecked)}

        populateAutoTextView(
            requireContext(),
            regionListSpinner,
            regions.getAll().distinct().toTypedArray()
        )
        populateAutoTextView(
            requireContext(),
            schoolListSpinner,
            schools.getAll().toTypedArray()
        )

        gradeTenth.setOnClickListener { onRadioButtonClicked(it) }
        gradeEleventh.setOnClickListener { onRadioButtonClicked(it) }
        gradeTwelfth.setOnClickListener { onRadioButtonClicked(it) }
    }

    private fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> viewModel.grade = Grade.TENTH
            R.id.gradeEleventh -> viewModel.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> viewModel.grade = Grade.TWELFTH
        }
    }
}
