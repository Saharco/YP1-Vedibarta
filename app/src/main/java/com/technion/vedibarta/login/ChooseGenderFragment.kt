package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import kotlinx.android.synthetic.main.fragment_choose_gender.*

/**
 * A simple [Fragment] subclass.
 */
class ChooseGenderFragment : VedibartaFragment() {


    private val TAG = "GenderFragment@Setup"
    private val BORDER_WIDTH = 10
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

        (activity as UserSetupActivity).setupStudent.gender = Gender.FEMALE
    }

    private fun onButtonMaleClickListener() {

        imageMale.borderWidth = BORDER_WIDTH
        imageMale.borderColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
        imageFemale.borderWidth = 0

        (activity as UserSetupActivity).setupStudent.gender = Gender.MALE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAndInitViews(view)

    }

    override fun setupAndInitViews(v: View) {
        super.setupAndInitViews(v)
        Glide.with(context!!).load(R.drawable.ic_photo_default_profile_man).into(imageMale)
        Glide.with(context!!).load(R.drawable.ic_photo_default_profile_girl).into(imageFemale)
        imageMale.setOnClickListener { onButtonMaleClickListener() }
        imageFemale.setOnClickListener { onButtonFemaleClickListener() }

        when ((activity as UserSetupActivity).setupStudent.gender) {
            Gender.MALE -> {
                imageMale.borderWidth = BORDER_WIDTH
                imageMale.borderColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
                imageFemale.borderWidth = 0
            }
            Gender.FEMALE -> {
                imageFemale.borderWidth = BORDER_WIDTH
                imageFemale.borderColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
                imageMale.borderWidth = 0
            }
            Gender.NONE -> {
            }
        }
    }

}
