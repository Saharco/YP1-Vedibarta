package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment

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
        val view = inflater.inflate(R.layout.fragment_choose_characteristics, container, false)
        val characteristics = if ((activity as UserSetupActivity).setupStudent.gender != Gender.FEMALE)
            resources.getStringArray(R.array.characteristicsMale_hebrew)
        else
            resources.getStringArray(R.array.characteristicsFemale_hebrew)

        val table = view.findViewById(R.id.searchCharacteristics) as TableLayout

        populateCharacteristicsTable(activity as UserSetupActivity, table, characteristics.toList().shuffled().toTypedArray(), (activity as UserSetupActivity).setupStudent)

        return view
    }

}
