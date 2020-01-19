package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*

/**
 * A simple [Fragment] subclass.
 */
class ChooseCharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharFragment@Setup"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_characteristics, container, false)
    }

    override fun onStart() {
        super.onStart()
        val characteristics = if ((activity as UserSetupActivity).setupStudent.gender != Gender.FEMALE)
            resources.getStringArray(R.array.characteristicsMale_hebrew)
        else
            resources.getStringArray(R.array.characteristicsFemale_hebrew)
        populateCharacteristicsTable(activity as UserSetupActivity, searchCharacteristics, characteristics.toList().shuffled().toTypedArray(), (activity as UserSetupActivity).setupStudent)
    }

}
