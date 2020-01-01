package com.technion.vedibarta.userProfile


import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.get

import com.technion.vedibarta.R
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.dpToPx
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
            resources.getStringArray(R.array.characteristicsMale_hebrew)
        else
            resources.getStringArray(R.array.characteristicsFemale_hebrew)

        val table = view.findViewById(R.id.editCharacteristicsTable) as TableLayout
        populateCharacteristicsTable(this.context!!, table, characteristics, student!!)
        return view
    }
}
