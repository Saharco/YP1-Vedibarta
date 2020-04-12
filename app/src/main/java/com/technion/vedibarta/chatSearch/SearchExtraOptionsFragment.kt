package com.technion.vedibarta.chatSearch


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat

import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_search_extra_options.*
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.Resource


/**
 * A simple [Fragment] subclass.
 */
class SearchExtraOptionsFragment : VedibartaFragment() {

    private val TAG = "ExtraFragment@Search"

    // Tag -> (schoolName, SchoolRegion)
    lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>

    private lateinit var schoolTextViewAuto: AutoCompleteTextView
    private lateinit var regionTextViewAuto: AutoCompleteTextView

    private lateinit var argumentTransfer: ArgumentTransfer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        argumentTransfer = context as? ArgumentTransfer
            ?: throw ClassCastException("$context must implement ${ArgumentTransfer::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_extra_options, container, false)
        setupAndInitViews(view)
        return view
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as ChatSearchActivity)
        regionTextViewAuto.clearFocus()
        schoolTextViewAuto.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int) {

        val nameList =
            schoolAndRegionMap.filter { it.value.first == schoolTextViewAuto.adapter.getItem(position) }
                .values.toMutableList()
        val region = nameList[position % nameList.size].second
        val schoolName = schoolTextViewAuto.text.toString()

        regionTextViewAuto.text = SpannableStringBuilder(region)
        (activity as ChatSearchActivity).chosenSchool = schoolName
        (activity as ChatSearchActivity).chosenRegion = region

        VedibartaActivity.hideKeyboard(activity as ChatSearchActivity)
    }

    private fun onRegionSelectedListener(position: Int) {
        schoolTextViewAuto.text = SpannableStringBuilder("")
        val region = regionTextViewAuto.adapter.getItem(position).toString()
        (activity as ChatSearchActivity).chosenRegion = region
        (activity as ChatSearchActivity).chosenSchool = null

        val schoolList = schoolAndRegionMap.filter { it.value.second == region }.values.toMutableList().unzip()
                .first.toTypedArray()

        populateAutoTextView(activity as ChatSearchActivity, schoolTextViewAuto, schoolList)
    }

    private fun schoolOnCheckedChanged(isChecked: Boolean) {
        schoolTextViewAuto.text = SpannableStringBuilder("")
        VedibartaActivity.hideKeyboard(activity as ChatSearchActivity)
        if (isChecked) {
            schoolListSpinner.visibility = View.VISIBLE
        } else {
            schoolListSpinner.visibility = View.GONE
            (activity as ChatSearchActivity).chosenSchool = null
        }
    }

    private fun regionOnCheckedChanged(isChecked: Boolean) {
        VedibartaActivity.hideKeyboard(activity as ChatSearchActivity)

        if (isChecked) {
            regionListSpinner.visibility = View.VISIBLE
        } else {
            regionListSpinner.visibility = View.GONE
            (activity as ChatSearchActivity).chosenRegion = null
            regionTextViewAuto.text = SpannableStringBuilder("")
        }
    }

    override fun setupAndInitViews(v: View) {
        super.setupAndInitViews(v)
        val argMap = argumentTransfer.getArgs()
        val act  = argMap["activity"] as Activity
        val schoolsNameTask = argMap["schoolsNameTask"] as Task<Resource>
        val regionsNameTask = argMap["regionsNameTask"] as Task<Resource>
        Tasks.whenAll(regionsNameTask, schoolsNameTask)
            .addOnSuccessListener(act){
                schoolAndRegionMap = (act as ChatSearchActivity).schoolTags.zip(act.schoolsNameTask.result!!.getAll().zip(regionsNameTask.result!!.getAll())).toMap()

                //---Switch Views---
                val schoolSwitch : SwitchCompat= v.findViewById(R.id.schoolFilterSwitch)
                val regionSwitch : SwitchCompat= v.findViewById(R.id.regionFilterSwitch)

                schoolSwitch.setOnCheckedChangeListener { _, isChecked -> schoolOnCheckedChanged(isChecked) }
                regionSwitch.setOnCheckedChangeListener { _, isChecked -> regionOnCheckedChanged(isChecked) }

                //---DropDownList Views---
                schoolTextViewAuto = v.findViewById(R.id.schoolListSpinner)
                regionTextViewAuto = v.findViewById(R.id.regionListSpinner)

                schoolTextViewAuto.setOnItemClickListener { _, _, pos, _ -> onSchoolSelectedListener(pos)}
                regionTextViewAuto.setOnItemClickListener { _, _, pos, _ -> onRegionSelectedListener(pos)}

                regionTextViewAuto.doOnTextChanged { text, _, _, _ ->
                    populateAutoTextView(act, schoolTextViewAuto, act.schoolsNameTask.result!!.getAll().toTypedArray())
                    (activity as ChatSearchActivity).chosenRegion = if (text.toString() == "") null else text.toString()}

                schoolTextViewAuto.doOnTextChanged { text, _, _, _ -> (activity as ChatSearchActivity).chosenSchool = if (text.toString() == "") null else text.toString() }

                populateAutoTextView(activity as VedibartaActivity, regionTextViewAuto, regionsNameTask.result!!.getAll().distinct().toTypedArray())
                populateAutoTextView(activity as VedibartaActivity, schoolTextViewAuto, schoolsNameTask.result!!.getAll().toTypedArray())
            }
    }
}
