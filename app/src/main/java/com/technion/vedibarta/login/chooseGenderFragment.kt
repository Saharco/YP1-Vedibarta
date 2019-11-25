package com.technion.vedibarta.login


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView

import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.activity_chat_search.*
import kotlinx.android.synthetic.main.activity_user_setup.*
import kotlinx.android.synthetic.main.activity_user_setup.toolbarTitle
import kotlinx.android.synthetic.main.fragment_choose_gender.*

/**
 * A simple [Fragment] subclass.
 */
class chooseGenderFragment : Fragment() {


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
        buttonNext = view.findViewById(R.id.buttonNext)

        cardViewMale = view.findViewById(R.id.cardViewMale)
        cardViewFemale = view.findViewById(R.id.cardViewFemale)

        buttonMale.setOnClickListener { onButtonMaleClickListener() }
        buttonFemale.setOnClickListener { onButtonFemaleClickListener() }

        cardViewMale.setOnClickListener { onButtonMaleClickListener() }
        cardViewFemale.setOnClickListener { onButtonFemaleClickListener() }

        buttonNext.setOnClickListener { onButtonNextClickListener(it) }
        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as userSetupActivity).toolbarTitle.text = resources.getString(R.string.user_setup_title)

    }

    private fun onButtonFemaleClickListener() {
        cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        cardViewMale.setCardBackgroundColor(resources.getColor(R.color.background))

        (activity as userSetupActivity).chosenGender = "Female"
        if (!(activity as userSetupActivity).userSetupContainer.pageEnabled)
            buttonNext.visibility = View.VISIBLE
    }

    private fun onButtonMaleClickListener() {
        cardViewMale.setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        cardViewFemale.setCardBackgroundColor(resources.getColor(R.color.background))

        (activity as userSetupActivity).chosenGender = "Male"
        if (!(activity as userSetupActivity).userSetupContainer.pageEnabled)
            buttonNext.visibility = View.VISIBLE

    }

    private fun onButtonNextClickListener(v: View) {
        v.visibility = View.GONE
        (activity as userSetupActivity).userSetupContainer.currentItem = 1
        (activity as userSetupActivity).userSetupContainer.setPagingEnabled(true)
    }


}
