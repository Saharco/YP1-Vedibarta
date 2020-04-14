package com.technion.vedibarta.userProfile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout

import com.technion.vedibarta.R
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student
import com.technion.vedibarta.utilities.VedibartaFragment

class ProfileEditCharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharFragment@Edit"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_profile_edit_characteristics, container,
            false
        )
        val characteristics : Array<String> = if (student!!.gender != Gender.FEMALE)
            resources.getStringArray(R.array.characteristicsMale)
        else
            resources.getStringArray(R.array.characteristicsFemale)

        val table = view.findViewById(R.id.editCharacteristicsTable) as TableLayout

        populateCharacteristicsTable(this.context!!, table, characteristics.toList().shuffled().toTypedArray(), student!!)
        return view
    }
}
