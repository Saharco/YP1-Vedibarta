package com.technion.vedibarta.login


import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText

import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity

/**
 * A simple [Fragment] subclass.
 */
class ChooseExtraOptionsFragment : Fragment() {

    private val TAG = "ExtraFragment@Setup"


    // Tag -> (schoolName, SchoolRegion)
    private lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>

    private lateinit var schoolTextViewAuto: AutoCompleteTextView
    private lateinit var regionTextViewAuto: AutoCompleteTextView

    private lateinit var schoolAdapter: ArrayAdapter<String>
    private lateinit var regionAdapter: ArrayAdapter<String>

    private lateinit var firstName: TextInputEditText
    private lateinit var lastName: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_extra_options, container, false)
        val act = (activity as UserSetupActivity)

        act.schoolsName = resources.getStringArray(R.array.schoolNameList)
        act.regionsName = resources.getStringArray(R.array.regionNameList).toList().distinct().toTypedArray()
        act.schoolTags = resources.getIntArray(R.array.schoolTagList).toTypedArray()

        firstName = view.findViewById(R.id.textFieldFirstName)
        lastName = view.findViewById(R.id.textFieldLastName)

        firstName.doOnTextChanged { text, _, _, _ -> act.chosenFirstName = text.toString() }
        lastName.doOnTextChanged { text, _, _, _ -> act.chosenLastName = text.toString() }

        schoolTextViewAuto = view.findViewById(R.id.schoolListSpinner)
        regionTextViewAuto = view.findViewById(R.id.regionListSpinner)

        schoolAndRegionMap = act.schoolTags.zip(act.schoolsName.zip(resources.getStringArray(R.array.regionNameList))).toMap()


        schoolTextViewAuto.setOnItemClickListener { _, _, position, _ -> onSchoolSelectedListener(
            position
        ) }
        regionTextViewAuto.setOnItemClickListener { _, _, pos, _ -> onRegionSelectedListener(pos) }

        regionTextViewAuto.doOnTextChanged { _, _, _, _ -> populateSchoolAutoCompleteText() }

        populateSchoolAutoCompleteText()
        populateRegionAutoCompleteText()

        initViews(view)

        return view
    }

    private fun TextView.markRequired() {
        text = buildSpannedString {
            append(text)
            color(Color.RED) { append(" *") } // Mind the space prefix.
        }
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
        schoolTextViewAuto.clearFocus()
        regionTextViewAuto.clearFocus()
        firstName.clearFocus()
        lastName.clearFocus()
    }

    private fun populateSchoolAutoCompleteText() {

        Log.d(TAG, "${activity!!.applicationContext}")

        schoolAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line, (activity as UserSetupActivity).schoolsName
        )
        schoolTextViewAuto.setAdapter(schoolAdapter)
    }

    private fun populateRegionAutoCompleteText() {
        regionAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line, (activity as UserSetupActivity).regionsName
        )
        regionTextViewAuto.setAdapter(regionAdapter)
    }

    private fun onSchoolSelectedListener(
        position: Int
    ) {

        val nameList =
            schoolAndRegionMap.filter { it.value.first == schoolAdapter.getItem(position) }
                .values.toMutableList()
        val region = nameList[position % nameList.size].second
        val schoolName = schoolTextViewAuto.text.toString()

        regionTextViewAuto.text = SpannableStringBuilder(region)
        (activity as UserSetupActivity).setupStudent.school = schoolName
        (activity as UserSetupActivity).setupStudent.region = region

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
        populateSchoolAutoCompleteText()
    }

    private fun onRegionSelectedListener(position: Int) {
        schoolTextViewAuto.text = SpannableStringBuilder("")
        val region = regionAdapter.getItem(position).toString()
        (activity as UserSetupActivity).setupStudent.region = region
        (activity as UserSetupActivity).setupStudent.school = ""

        val schoolList =
            schoolAndRegionMap.filter { it.value.second == region }.values.toMutableList().unzip()
                .first.toTypedArray()
        schoolAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line, schoolList
        )
        schoolTextViewAuto.setAdapter(schoolAdapter)

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    private fun initViews(v: View) {
        val act = (activity as UserSetupActivity)

        v.findViewById<TextView>(R.id.textViewFirstNameTitle).markRequired()
        v.findViewById<TextView>(R.id.textViewLastNameTitle).markRequired()
        v.findViewById<TextView>(R.id.schoolFilterSwitchText).markRequired()
        v.findViewById<TextView>(R.id.regionFilterSwitchText).markRequired()

        Log.d(TAG, "Init Views")

        schoolTextViewAuto.text = SpannableStringBuilder(act.setupStudent.school)
        regionTextViewAuto.text = SpannableStringBuilder(act.setupStudent.region)
    }

}
