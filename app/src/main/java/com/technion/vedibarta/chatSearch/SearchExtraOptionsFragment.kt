package com.technion.vedibarta.chatSearch


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat

import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_search_extra_options.*
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import com.technion.vedibarta.utilities.VedibartaActivity


/**
 * A simple [Fragment] subclass.
 */
class SearchExtraOptionsFragment : Fragment() {

    private val TAG = "ExtraFragment@Search"

    lateinit var schoolSwitch: SwitchCompat
    lateinit var regionSwitch: SwitchCompat

    // Tag -> (schoolName, SchoolRegion)
    lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>

    private lateinit var schoolTextViewAuto: AutoCompleteTextView
    private lateinit var regionTextViewAuto: AutoCompleteTextView

    lateinit var schoolAdapter: ArrayAdapter<String>
    lateinit var regionAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_extra_options, container, false)
        (activity as ChatSearchActivity).schoolsName =
            resources.getStringArray(R.array.schoolNameList)
        (activity as ChatSearchActivity).regionsName =
            resources.getStringArray(R.array.regionNameList).toList().distinct().toTypedArray()
        (activity as ChatSearchActivity).schoolTags =
            resources.getIntArray(R.array.schoolTagList).toTypedArray()

        schoolSwitch = view.findViewById(R.id.schoolFilterSwitch)
        regionSwitch = view.findViewById(R.id.regionFilterSwitch)

        schoolTextViewAuto = view.findViewById(R.id.schoolListSpinner)
        regionTextViewAuto = view.findViewById(R.id.regionListSpinner)

        schoolAndRegionMap =
            (activity as ChatSearchActivity).schoolTags.zip(
                (activity as ChatSearchActivity).schoolsName.zip(
                    resources.getStringArray(R.array.regionNameList)
                )
            )
                .toMap()


        schoolSwitch.setOnCheckedChangeListener { _, isChecked -> schoolOnCheckedChanged(isChecked) }
        regionSwitch.setOnCheckedChangeListener { _, isChecked -> regionOnCheckedChanged(isChecked) }

        schoolTextViewAuto.setOnItemClickListener { _, _, position, _ ->
            onSchoolSelectedListener(
                position
            )
        }
        regionTextViewAuto.setOnItemClickListener { _, _, pos, _ -> onRegionSelectedListener(pos) }

        regionTextViewAuto.doOnTextChanged { text, _, _, _ ->
            populateSchoolAutoCompleteText()
            (activity as ChatSearchActivity).chosenRegion = text.toString()
        }

        schoolTextViewAuto.doOnTextChanged { text, _, _, _ ->
            (activity as ChatSearchActivity).chosenSchool = text.toString()
        }

        populateSchoolAutoCompleteText()
        populateRegionAutoCompleteText()

        return view
    }

    private fun populateSchoolAutoCompleteText() {

        Log.d(TAG, "${activity!!.applicationContext}")

        schoolAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line,
            (activity as ChatSearchActivity).schoolsName
        )
        schoolTextViewAuto.setAdapter(schoolAdapter)
    }

    private fun populateRegionAutoCompleteText() {
        regionAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line,
            (activity as ChatSearchActivity).regionsName
        )
        regionTextViewAuto.setAdapter(regionAdapter)
    }

    private fun schoolOnCheckedChanged(isChecked: Boolean) {
        populateSchoolAutoCompleteText()
        schoolTextViewAuto.text = SpannableStringBuilder("")
        if (isChecked) {
            schoolListSpinner.visibility = View.VISIBLE
        } else {
            schoolListSpinner.visibility = View.GONE
            (activity as ChatSearchActivity).chosenSchool = null
        }
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as ChatSearchActivity)
        regionTextViewAuto.clearFocus()
        schoolTextViewAuto.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int) {

        val nameList =
            schoolAndRegionMap.filter { it.value.first == schoolAdapter.getItem(position) }
                .values.toMutableList()
        val region = nameList[position % nameList.size].second
        val schoolName = schoolTextViewAuto.text.toString()

        regionTextViewAuto.text = SpannableStringBuilder(region)
        (activity as ChatSearchActivity).chosenSchool = schoolName
        (activity as ChatSearchActivity).chosenRegion = region
        populateSchoolAutoCompleteText()
    }

    private fun onRegionSelectedListener(position: Int) {
        schoolTextViewAuto.text = SpannableStringBuilder("")
        val region = regionAdapter.getItem(position).toString()
        (activity as ChatSearchActivity).chosenRegion = region
        (activity as ChatSearchActivity).chosenSchool = null

        val schoolList =
            schoolAndRegionMap.filter { it.value.second == region }.values.toMutableList().unzip()
                .first.toTypedArray()
        schoolAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line, schoolList
        )
        schoolTextViewAuto.setAdapter(schoolAdapter)

    }

    private fun regionOnCheckedChanged(isChecked: Boolean) {
        populateSchoolAutoCompleteText()
        if (isChecked) {
            regionListSpinner.visibility = View.VISIBLE
        } else {
            regionListSpinner.visibility = View.GONE
            (activity as ChatSearchActivity).chosenRegion = null
            regionTextViewAuto.text = SpannableStringBuilder("")
        }

    }

}
