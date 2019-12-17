package com.technion.vedibarta.login


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class ChooseExtraOptionsFragment : VedibartaFragment() {

    private val TAG = "ExtraFragment@Setup"


    // Tag -> (schoolName, SchoolRegion)
    private lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>

    private lateinit var schoolTextViewAuto: AutoCompleteTextView
    private lateinit var regionTextViewAuto: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_extra_options, container, false)
        setupAndInitViews(view)
        return view
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
        schoolTextViewAuto.clearFocus()
        regionTextViewAuto.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int, view: View) {

        val nameList = schoolAndRegionMap.filter {
                it.value.first == schoolTextViewAuto.adapter.getItem(position)}.values.toMutableList()
        val region = nameList[position % nameList.size].second
        val schoolName = schoolTextViewAuto.text.toString()

        regionTextViewAuto.text = SpannableStringBuilder(region)
        (activity as UserSetupActivity).setupStudent.school = schoolName
        (activity as UserSetupActivity).setupStudent.region = region

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    private fun onRegionSelectedListener(position: Int, view: View) {

        schoolTextViewAuto.text = SpannableStringBuilder("")
        val region = regionTextViewAuto.adapter.getItem(position).toString()
        (activity as UserSetupActivity).setupStudent.region = region
        (activity as UserSetupActivity).setupStudent.school = ""

        val schoolList =
            schoolAndRegionMap.filter { it.value.second == region }.values.toMutableList().unzip()
                .first.toTypedArray()

        populateAutoTextView(activity as UserSetupActivity, schoolTextViewAuto, schoolList)

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    override fun setupAndInitViews(v: View) {
        super.setupAndInitViews(v)
        val act = (activity as UserSetupActivity)

        schoolAndRegionMap =
            act.schoolTags.zip(act.schoolsName.zip(resources.getStringArray(R.array.regionNameList)))
                .toMap()

        // ---Student Name Views---
        val firstName: TextInputEditText = v.findViewById(R.id.textFieldFirstName)
        val lastName: TextInputEditText = v.findViewById(R.id.textFieldLastName)

        firstName.doOnTextChanged { text, _, _, _ -> act.chosenFirstName = text.toString() }
        lastName.doOnTextChanged { text, _, _, _ -> act.chosenLastName = text.toString() }

        //---DropDownList Views---
        schoolTextViewAuto = v.findViewById(R.id.schoolListSpinner)
        regionTextViewAuto = v.findViewById(R.id.regionListSpinner)

        schoolTextViewAuto.setOnItemClickListener { _, _, position, _ -> onSchoolSelectedListener(position, v)}
        regionTextViewAuto.setOnItemClickListener { _, _, position, _ -> onRegionSelectedListener(position, v)}

        regionTextViewAuto.doOnTextChanged { _, _, _, _ ->
            populateAutoTextView(act, schoolTextViewAuto, act.schoolsName)
        }

        //---Populate DropDownLists---
        populateAutoTextView(act, schoolTextViewAuto, act.schoolsName)
        populateAutoTextView(act, regionTextViewAuto, act.regionsName)


        v.findViewById<TextView>(R.id.textViewFirstNameTitle).markRequired()
        v.findViewById<TextView>(R.id.textViewLastNameTitle).markRequired()
        v.findViewById<TextView>(R.id.schoolFilterSwitchText).markRequired()
        v.findViewById<TextView>(R.id.regionFilterSwitchText).markRequired()

        Log.d(TAG, "Init Views")

        schoolTextViewAuto.text = SpannableStringBuilder(act.setupStudent.school)
        regionTextViewAuto.text = SpannableStringBuilder(act.setupStudent.region)
    }

}
