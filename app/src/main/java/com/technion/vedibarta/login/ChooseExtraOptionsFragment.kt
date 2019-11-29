package com.technion.vedibarta.login


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity

/**
 * A simple [Fragment] subclass.
 */
class ChooseExtraOptionsFragment : Fragment() {

    private val TAG = "ExtraFragment@Setup"

    private val FIRST_NAME = "firstname"
    private val LAST_NAME = "lastname"

    private lateinit var schoolsName: Array<String>
    private lateinit var regionsName: Array<String>
    private lateinit var schoolTags: Array<Int>


    // Tag -> (schoolName, SchoolRegion)
    private lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>

    private lateinit var schoolTextViewAuto: AutoCompleteTextView
    private lateinit var regionTextViewAuto: AutoCompleteTextView

    private lateinit var schoolAdapter: ArrayAdapter<String>
    private lateinit var regionAdapter: ArrayAdapter<String>

    private lateinit var firstName : TextInputEditText
    private lateinit var lastName : TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {
        val act = (activity as UserSetupActivity)

        if(savedInstanceState != null){
            act.chosenFirstName = savedInstanceState.getString(FIRST_NAME)!!
            act.chosenLastName = savedInstanceState.getString(LAST_NAME)!!
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_extra_options, container, false)

        schoolsName = resources.getStringArray(R.array.schoolNameList)
        regionsName =
            resources.getStringArray(R.array.regionNameList).toList().distinct().toTypedArray()
        schoolTags = resources.getIntArray(R.array.schoolTagList).toTypedArray()

        firstName = view.findViewById(R.id.textFieldFirstName)
        lastName = view.findViewById(R.id.textFieldLastName)

        schoolTextViewAuto = view.findViewById(R.id.schoolListSpinner)
        regionTextViewAuto = view.findViewById(R.id.regionListSpinner)

        schoolAndRegionMap =
            schoolTags.zip(schoolsName.zip(resources.getStringArray(R.array.regionNameList)))
                .toMap()


        schoolTextViewAuto.setOnItemClickListener { v, _, position, _ -> onSchoolSelectedListener(v, position) }
        regionTextViewAuto.setOnItemClickListener { v, _, pos, _ -> onRegionSelectedListener(v, pos) }

        populateSchoolAutoCompleteText()
        populateRegionAutoCompleteText()

        initViews()

        return view
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
            android.R.layout.simple_dropdown_item_1line, schoolsName
        )
        schoolTextViewAuto.setAdapter(schoolAdapter)
    }

    private fun populateRegionAutoCompleteText() {
        regionAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line, regionsName
        )
        regionTextViewAuto.setAdapter(regionAdapter)
    }

    override fun onSaveInstanceState(outState: Bundle) {

        val act = (activity as UserSetupActivity)

        outState.putString(FIRST_NAME, act.chosenFirstName)
        outState.putString(LAST_NAME, act.chosenLastName)

        super.onSaveInstanceState(outState)

    }

    private fun onSchoolSelectedListener(
        v: AdapterView<*>,
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

        v.clearFocus()

        populateSchoolAutoCompleteText()
    }

    private fun onRegionSelectedListener(v: AdapterView<*>, position: Int) {
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

        v.clearFocus()

    }

    private fun initViews() {
        val act = (activity as UserSetupActivity)

        firstName.text =  SpannableStringBuilder(act.chosenFirstName)
        lastName.text = SpannableStringBuilder(act.chosenLastName)

        schoolTextViewAuto.text = SpannableStringBuilder(act.setupStudent.school)
        regionTextViewAuto.text = SpannableStringBuilder(act.setupStudent.region)
    }

}
