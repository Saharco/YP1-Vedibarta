package com.technion.vedibarta.login

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Unfilled
import com.technion.vedibarta.R
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import kotlinx.android.synthetic.main.fragment_choose_personal_info.*

/**
 * A simple [Fragment] subclass.
 */
class ChoosePersonalInfoFragment : VedibartaFragment() {

    companion object {
        private const val TAG = "PersonalInfoFragment"
        private const val BORDER_WIDTH = 10
    }

    private lateinit var schoolAndRegionMap: Map<String, String>

    private val viewModel: UserSetupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_personal_info, container, false)
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
        schoolListSpinner.clearFocus()
        regionListSpinner.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int) {
        val schoolName = schoolListSpinner.adapter.getItem(position).toString()
        val region = schoolAndRegionMap[schoolName].toString()

        regionListSpinner.text = SpannableStringBuilder(region)
        viewModel.chosenSchool = Filled(schoolName)
        viewModel.chosenRegion = Filled(region)

        VedibartaActivity.hideKeyboard(activity as UserSetupActivity)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAndInitViews(view)
    }

    override fun setupAndInitViews(v: View) {
        super.setupAndInitViews(v)
        genderInit()
        extraOptionsInit(v)
    }

    private fun genderInit() {
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_man).into(imageMale)
        Glide.with(requireContext()).load(R.drawable.ic_photo_default_profile_girl).into(imageFemale)
        imageMale.setOnClickListener { viewModel.setGender(Gender.MALE) }
        imageFemale.setOnClickListener { viewModel.setGender(Gender.FEMALE) }

        viewModel.gender.observe(viewLifecycleOwner) {
            when (it) {
                Gender.MALE -> {
                    imageMale.borderWidth = BORDER_WIDTH
                    imageMale.borderColor = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
                    imageFemale.borderWidth = 0
                    textOptionMale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorAccentDark
                        )
                    )
                    textOptionFemale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.textPrimary
                        )
                    )
                }
                Gender.FEMALE -> {
                    imageFemale.borderWidth = BORDER_WIDTH
                    imageFemale.borderColor =
                        ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
                    imageMale.borderWidth = 0
                    textOptionFemale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorAccentDark
                        )
                    )
                    textOptionMale.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.textPrimary
                        )
                    )
                }
                Gender.NONE -> {
                }
            }
        }
    }

    private fun extraOptionsInit(v: View) {
        val schools = StudentResources.schools
        val regions = StudentResources.regions
        schoolAndRegionMap = schools.getAll().zip(regions.getAll()).toMap()

        // ---Student Name Views---
        val firstName: TextInputEditText = v.findViewById(R.id.textFieldFirstName)
        val lastName: TextInputEditText = v.findViewById(R.id.textFieldLastName)

        textFieldFirstName.doOnTextChanged { text, _, _, _ ->
            when {
                text!!.matches(resources.getString(R.string.allowed_letters_regex).toRegex()) ->
                    viewModel.chosenFirstName = Filled(text.toString())
                text.isBlank() ->
                    viewModel.chosenFirstName = Unfilled
                else -> firstName.text = SpannableStringBuilder("")
            }
        }

        textFieldLastName.doOnTextChanged { text, _, _, _ ->
            when {
                text!!.matches(resources.getString(R.string.allowed_letters_regex).toRegex()) ->
                    viewModel.chosenLastName = Filled(text.toString())
                text.isBlank() ->
                    viewModel.chosenLastName = Unfilled
                else -> lastName.text = SpannableStringBuilder("")
            }
        }

        schoolListSpinner.setOnItemClickListener { _, _, position, _ ->
            onSchoolSelectedListener(position)
        }

        regionListSpinner.setOnItemClickListener { _, _, position, _ ->
            onRegionSelectedListener(position)
        }

        schoolListSpinner.doOnTextChanged { text, _, _, _ ->
            viewModel.chosenSchool = Filled(text.toString())
        }

        regionListSpinner.doOnTextChanged { text, _, _, _ ->
            populateAutoTextView(
                requireContext(),
                schoolListSpinner,
                schools.getAll().toTypedArray()
            )
            viewModel.chosenRegion = Filled(text.toString())
        }

        //---Populate DropDownLists---
        populateAutoTextView(
            requireContext(),
            schoolListSpinner,
            schools.getAll().toTypedArray()
        )
        populateAutoTextView(
            requireContext(),
            regionListSpinner,
            regions.getAll().distinct().toTypedArray()
        )

        textViewFirstNameTitle.markRequired()
        textViewLastNameTitle.markRequired()
        schoolFilterSwitchText.markRequired()
        regionFilterSwitchText.markRequired()
        gradeText.markRequired()

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

        when (val it = viewModel.chosenSchool) {
            is Filled -> schoolListSpinner.text = SpannableStringBuilder(it.text)
        }

        when (val it = viewModel.chosenRegion) {
            is Filled -> regionListSpinner.text = SpannableStringBuilder(it.text)
        }

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
