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
import kotlinx.android.synthetic.main.fragment_choose_extra_options.*
import kotlinx.android.synthetic.main.fragment_choose_extra_options.view.*
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout


/**
 * A simple [Fragment] subclass.
 */
class ChooseExtraOptionsFragment : VedibartaFragment() {

    private val TAG = "ExtraFragment@Setup"


    // Tag -> (schoolName, SchoolRegion)
    private lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_extra_options, container, false)
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
        schoolListSpinner.clearFocus()
        regionListSpinner.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int, view: View) {

        val nameList = schoolAndRegionMap.filter {
            it.value.first == schoolListSpinner.adapter.getItem(position)
        }.values.toMutableList()
        val region = nameList[position % nameList.size].second
        val schoolName = schoolListSpinner.text.toString()

        regionListSpinner.text = SpannableStringBuilder(region)
        (activity as UserSetupActivity).setupStudent.school = schoolName
        (activity as UserSetupActivity).setupStudent.region = region

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    private fun onRegionSelectedListener(position: Int, view: View) {

        schoolListSpinner.text = SpannableStringBuilder("")
        val region = regionListSpinner.adapter.getItem(position).toString()
        (activity as UserSetupActivity).setupStudent.region = region
        (activity as UserSetupActivity).setupStudent.school = ""

        val schoolList =
            schoolAndRegionMap.filter { it.value.second == region }.values.toMutableList().unzip()
                .first.toTypedArray()

        populateAutoTextView(activity as UserSetupActivity, schoolListSpinner, schoolList)

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAndInitViews(view)
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

        textFieldFirstName.doOnTextChanged { text, _, _, _ ->
            if (text!!.matches(resources.getString(R.string.allowed_letters_regex).toRegex()) || text.isBlank())
                act.chosenFirstName = text.toString()
            else {
                firstName.text = SpannableStringBuilder("")
            }
        }

        textFieldLastName.doOnTextChanged { text, _, _, _ ->
            if (text!!.matches(resources.getString(R.string.allowed_letters_regex).toRegex()) || text.isBlank())
                act.chosenLastName = text.toString()
            else {
                lastName.text = SpannableStringBuilder("")
            }
        }

        schoolListSpinner.setOnItemClickListener { _, _, position, _ ->
            onSchoolSelectedListener(
                position,
                v
            )
        }
        regionListSpinner.setOnItemClickListener { _, _, position, _ ->
            onRegionSelectedListener(
                position,
                v
            )
        }

        regionListSpinner.doOnTextChanged { _, _, _, _ ->
            populateAutoTextView(act, schoolListSpinner, act.schoolsName)
        }

        //---Populate DropDownLists---
        populateAutoTextView(act, schoolListSpinner, act.schoolsName)
        populateAutoTextView(act, regionListSpinner, act.regionsName)


        textViewFirstNameTitle.markRequired()
        textViewLastNameTitle.markRequired()
        schoolFilterSwitchText.markRequired()
        regionFilterSwitchText.markRequired()

        Log.d(TAG, "Init Views")

        personalInfoButton.setOnClickListener {
            if (cardViewNameBody.visibility == View.VISIBLE)
                cardViewNameBody.visibility = View.GONE
            else
                cardViewNameBody.visibility = View.VISIBLE
        }


        schoolInfoButton.setOnClickListener {
            if (cardViewSchoolBody.visibility == View.VISIBLE)
                cardViewSchoolBody.visibility = View.GONE
            else
                cardViewSchoolBody.visibility = View.VISIBLE
        }
        schoolListSpinner.text = SpannableStringBuilder(act.setupStudent.school)
        regionListSpinner.text = SpannableStringBuilder(act.setupStudent.region)

    }

}
