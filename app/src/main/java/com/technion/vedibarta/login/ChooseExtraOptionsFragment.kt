package com.technion.vedibarta.login


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.activity_user_setup.*

/**
 * A simple [Fragment] subclass.
 */
class ChooseExtraOptionsFragment : Fragment() {

    private val TAG = "ExtraFragment@Setup"

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


        schoolTextViewAuto.setOnItemClickListener { _, _, position, _ ->
            onSchoolSelectedListener(
                position
            )
        }
        regionTextViewAuto.setOnItemClickListener { _, _, pos, _ -> onRegionSelectedListener(pos) }
        populateSchoolAutoCompleteText()
        populateRegionAutoCompleteText()

        initViews()

        return view
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

    private fun onSchoolSelectedListener(position: Int) {

        val nameList =
            schoolAndRegionMap.filter { it.value.first == schoolAdapter.getItem(position) }
                .values.toMutableList()
        val region = nameList[position % nameList.size]!!.second
        val schoolName = schoolTextViewAuto.text.toString()

        regionTextViewAuto.text = SpannableStringBuilder(region)
        (activity as UserSetupActivity).chosenSchool = schoolName
        (activity as UserSetupActivity).chosenRegion = region
        populateSchoolAutoCompleteText()
    }

    private fun onRegionSelectedListener(position: Int) {
        schoolTextViewAuto.text = SpannableStringBuilder("")
        val region = regionAdapter.getItem(position).toString()
        (activity as UserSetupActivity).chosenRegion = region
        (activity as UserSetupActivity).chosenSchool = ""

        val schoolList =
            schoolAndRegionMap.filter { it.value.second == region }.values.toMutableList().unzip()
                .first.toTypedArray()
        schoolAdapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line, schoolList
        )
        schoolTextViewAuto.setAdapter(schoolAdapter)

    }

    private fun initViews() {
        val act = (activity as UserSetupActivity)

        firstName.text =  SpannableStringBuilder(act.chosenFirstName)
        lastName.text = SpannableStringBuilder(act.chosenLastName)

        schoolTextViewAuto.text = SpannableStringBuilder(act.chosenSchool)
        regionTextViewAuto.text = SpannableStringBuilder(act.chosenRegion)
    }

}
