package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class ChooseGenderFragment : VedibartaFragment() {


    private val TAG = "GenderFragment@Setup"

    lateinit var cardViewFemale: CardView
    lateinit var cardViewMale: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_gender, container, false)
        setupAndInitViews(view)
        return view
    }

    private fun onButtonFemaleClickListener() {

        cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        cardViewMale.setCardBackgroundColor(resources.getColor(R.color.background))

        (activity as UserSetupActivity).setupStudent.gender = Gender.FEMALE
    }

    private fun onButtonMaleClickListener() {

        cardViewMale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.background))

        (activity as UserSetupActivity).setupStudent.gender = Gender.MALE
    }

    override fun setupAndInitViews(v: View) {
        super.setupAndInitViews(v)

        //---Buttons---
        val buttonMale : AppCompatButton = v.findViewById(R.id.buttonMale)
        val buttonFemale : AppCompatButton = v.findViewById(R.id.buttonFemale)

        buttonMale.setOnClickListener { onButtonMaleClickListener() }
        buttonFemale.setOnClickListener { onButtonFemaleClickListener() }

        //---Card Views---
        cardViewMale = v.findViewById(R.id.cardViewMale)
        cardViewFemale = v.findViewById(R.id.cardViewFemale)

        cardViewMale.setOnClickListener { onButtonMaleClickListener() }
        cardViewFemale.setOnClickListener { onButtonFemaleClickListener() }


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
