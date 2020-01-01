package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class ChooseHobbiesFragment : VedibartaFragment() {

    private val TAG = "HobbiesFragment@login"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_hobbies, container, false)

        val hobbies = resources.getStringArray(R.array.hobbies)
        val table = view.findViewById(R.id.chooseHobbiesTable) as TableLayout

        populateHobbiesTable(activity as VedibartaActivity,table,hobbies,(activity as UserSetupActivity).setupStudent)

        return view
    }

}
