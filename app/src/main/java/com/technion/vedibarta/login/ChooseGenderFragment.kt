package com.technion.vedibarta.login


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView

import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*

/**
 * A simple [Fragment] subclass.
 */
class ChooseGenderFragment : Fragment() {


    private val TAG = "GenderFragment@Setup"

    lateinit var buttonMale: AppCompatButton
    lateinit var buttonFemale: AppCompatButton
    lateinit var buttonNext: AppCompatButton


    lateinit var cardViewFemale: CardView
    lateinit var cardViewMale: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_gender, container, false)

        buttonMale = view.findViewById(R.id.buttonMale)
        buttonFemale = view.findViewById(R.id.buttonFemale)

        cardViewMale = view.findViewById(R.id.cardViewMale)
        cardViewFemale = view.findViewById(R.id.cardViewFemale)

        buttonMale.setOnClickListener { onButtonMaleClickListener() }
        buttonFemale.setOnClickListener { onButtonFemaleClickListener() }

        cardViewMale.setOnClickListener { onButtonMaleClickListener() }
        cardViewFemale.setOnClickListener { onButtonFemaleClickListener() }


        initViews()

        return view
    }

    private fun onButtonFemaleClickListener() {
        val adapter = ((activity as UserSetupActivity).userSetupContainer.adapter as SectionsPageAdapter)

        cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        cardViewMale.setCardBackgroundColor(resources.getColor(R.color.background))

        Log.d(TAG, "Female Chosen")

        (activity as UserSetupActivity).setupStudent.gender = Gender.FEMALE
        if ((activity as UserSetupActivity).flag){
//            buttonNext.visibility = View.VISIBLE
            (activity as UserSetupActivity).flag = false
            (activity as UserSetupActivity).setupViewPager((activity as UserSetupActivity).userSetupContainer)
            (activity as UserSetupActivity).editTabs.setupWithViewPager((activity as UserSetupActivity).userSetupContainer)
        }
    }

    private fun onButtonMaleClickListener() {
        val adapter = ((activity as UserSetupActivity).userSetupContainer.adapter as SectionsPageAdapter)

        cardViewMale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.background))

        Log.d(TAG, "Male Chosen")

        (activity as UserSetupActivity).setupStudent.gender = Gender.MALE
        if ((activity as UserSetupActivity).flag){
//            buttonNext.visibility = View.VISIBLE
            (activity as UserSetupActivity).flag = false
            (activity as UserSetupActivity).setupViewPager((activity as UserSetupActivity).userSetupContainer)
            (activity as UserSetupActivity).editTabs.setupWithViewPager((activity as UserSetupActivity).userSetupContainer)
        }

    }

    private fun initViews() {
        when ((activity as UserSetupActivity).setupStudent.gender) {
            Gender.MALE -> {
                cardViewMale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
                cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.background))
            }
            Gender.FEMALE -> {
                cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
                cardViewMale.setCardBackgroundColor(resources.getColor(R.color.background))
            }
            Gender.NONE -> {
            }
        }
    }


}
