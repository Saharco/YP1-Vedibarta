package com.technion.vedibarta.login


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import kotlinx.android.synthetic.main.activity_user_setup.*
import kotlinx.android.synthetic.main.fragment_choose_gender.*

/**
 * A simple [Fragment] subclass.
 */
class ChooseGenderFragment : VedibartaFragment() {


    private val TAG = "GenderFragment@Setup"
    private val BORDER_WIDTH = 10
    // Tag -> (schoolName, SchoolRegion)
    private lateinit var schoolAndRegionMap: Map<Int, Pair<String, String>>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_gender, container, false)
    }

    private fun onButtonFemaleClickListener() {

        imageFemale.borderWidth = BORDER_WIDTH
        imageFemale.borderColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
        imageMale.borderWidth = 0
        textOptionMale.textSize = 24f
        textOptionFemale.textSize = 30f

        (activity as UserSetupActivity).setupStudent.gender = Gender.FEMALE
        changeCharacteristicsGender()
        reloadCharacteristics()
    }

    private fun onButtonMaleClickListener() {

        imageMale.borderWidth = BORDER_WIDTH
        imageMale.borderColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
        imageFemale.borderWidth = 0
        textOptionMale.textSize = 30f
        textOptionFemale.textSize = 24f

        (activity as UserSetupActivity).setupStudent.gender = Gender.MALE
        changeCharacteristicsGender()
        reloadCharacteristics()
    }

    private fun changeCharacteristicsGender() {
        val student = (activity as UserSetupActivity).setupStudent
        if (student.characteristics.isEmpty()) return

        val maleCharacteristics =
            context!!.resources.getStringArray(R.array.characteristicsMale_hebrew)
        val femaleCharacteristics =
            context!!.resources.getStringArray(R.array.characteristicsFemale_hebrew)
        val map: MutableMap<String, Boolean> = mutableMapOf()

        when (student.gender) {
            Gender.MALE -> {
                student.characteristics.forEach { (s, v) ->
                    map[maleCharacteristics[femaleCharacteristics.indexOf(s)]] =
                        v
                }
            }
            Gender.FEMALE -> {
                student.characteristics.forEach { (s, v) ->
                    map[femaleCharacteristics[maleCharacteristics.indexOf(s)]] =
                        v
                }
            }
            else -> {
            }
        }
        student.characteristics = map

    }

    private fun reloadCharacteristics() {
        val frg =
            ((activity as UserSetupActivity).userSetupContainer.adapter as SectionsPageAdapter)
                .getItem(1)
        val ft = fragmentManager!!.beginTransaction()
        ft.detach(frg)
        ft.attach(frg)
        ft.commit()
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

        genderInit(v)
        extraOptionsInit(v)
    }

    private fun genderInit(v: View) {
        Glide.with(context!!).load(R.drawable.ic_photo_default_profile_man).into(imageMale)
        Glide.with(context!!).load(R.drawable.ic_photo_default_profile_girl).into(imageFemale)
        imageMale.setOnClickListener { onButtonMaleClickListener() }
        imageFemale.setOnClickListener { onButtonFemaleClickListener() }

        when ((activity as UserSetupActivity).setupStudent.gender) {
            Gender.MALE -> {
                imageMale.borderWidth = BORDER_WIDTH
                imageMale.borderColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
                imageFemale.borderWidth = 0
                textOptionMale.setTextColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.colorAccentDark
                    )
                )
                textOptionFemale.setTextColor(ContextCompat.getColor(context!!, R.color.background))
            }
            Gender.FEMALE -> {
                imageFemale.borderWidth = BORDER_WIDTH
                imageFemale.borderColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
                imageMale.borderWidth = 0
                textOptionFemale.setTextColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.colorAccentDark
                    )
                )
                textOptionMale.setTextColor(ContextCompat.getColor(context!!, R.color.background))
            }
            Gender.NONE -> {
            }
        }
    }

    private fun extraOptionsInit(v: View) {

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

        personalInfoButton.setOnClickListener {
            if (cardViewNameBody.visibility == View.VISIBLE)
                cardViewNameBody.visibility = View.GONE
            else
                cardViewNameBody.visibility = View.VISIBLE
            nameArrowButton.switchState()
        }


        schoolInfoButton.setOnClickListener {
            if (cardViewSchoolBody.visibility == View.VISIBLE)
                cardViewSchoolBody.visibility = View.GONE
            else
                cardViewSchoolBody.visibility = View.VISIBLE
            schoolArrowButton.switchState()
        }
        schoolListSpinner.text = SpannableStringBuilder(act.setupStudent.school)
        regionListSpinner.text = SpannableStringBuilder(act.setupStudent.region)

        nameArrowButton.switchState()
        schoolArrowButton.switchState()

        nameArrowButton.setAnimationDuration(200)
        schoolArrowButton.setAnimationDuration(200)

        nameArrowButton.setOnClickListener {
            if (cardViewNameBody.visibility == View.VISIBLE)
                cardViewNameBody.visibility = View.GONE
            else
                cardViewNameBody.visibility = View.VISIBLE
            nameArrowButton.switchState()
        }

        schoolArrowButton.setOnClickListener {
            if (cardViewSchoolBody.visibility == View.VISIBLE)
                cardViewSchoolBody.visibility = View.GONE
            else
                cardViewSchoolBody.visibility = View.VISIBLE
            schoolArrowButton.switchState()
        }
    }

}
