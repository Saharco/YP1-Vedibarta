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
import androidx.core.widget.doOnTextChanged
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

        (activity as UserSetupActivity).schoolsName = resources.getStringArray(R.array.schoolNameList)
        (activity as UserSetupActivity).regionsName =
            resources.getStringArray(R.array.regionNameList).toList().distinct().toTypedArray()
        (activity as UserSetupActivity).schoolTags = resources.getIntArray(R.array.schoolTagList).toTypedArray()

        firstName = view.findViewById(R.id.textFieldFirstName)
        lastName = view.findViewById(R.id.textFieldLastName)

        firstName.doOnTextChanged { text, _, _, _ -> (activity as UserSetupActivity).chosenFirstName=text.toString() }
        lastName.doOnTextChanged { text, _, _, _ -> (activity as UserSetupActivity).chosenLastName=text.toString() }

        schoolTextViewAuto = view.findViewById(R.id.schoolListSpinner)
        regionTextViewAuto = view.findViewById(R.id.regionListSpinner)

        schoolAndRegionMap =
            (activity as UserSetupActivity).schoolTags.zip((activity as UserSetupActivity).schoolsName.zip(resources.getStringArray(R.array.regionNameList)))
                .toMap()


        schoolTextViewAuto.setOnItemClickListener { v, _, position, _ -> onSchoolSelectedListener(v, position) }
        regionTextViewAuto.setOnItemClickListener { v, _, pos, _ -> onRegionSelectedListener(v, pos) }

        regionTextViewAuto.doOnTextChanged { _, _, _, _ -> populateSchoolAutoCompleteText() }

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

    override fun onSaveInstanceState(outState: Bundle) {

        val act = (activity as UserSetupActivity)


        Log.d(TAG, "First Name: ${firstName.text}, Last Name: ${lastName.text}")
        outState.putString(FIRST_NAME,firstName.text.toString())
        outState.putString(LAST_NAME, lastName.text.toString())

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

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
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

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    private fun initViews() {
        val act = (activity as UserSetupActivity)


        Log.d(TAG, "Init Views")

//        firstName.text =  SpannableStringBuilder(act.chosenFirstName)
//        lastName.text = SpannableStringBuilder(act.chosenLastName)

        schoolTextViewAuto.text = SpannableStringBuilder(act.setupStudent.school)
        regionTextViewAuto.text = SpannableStringBuilder(act.setupStudent.region)
    }

}
