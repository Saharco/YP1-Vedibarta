package com.technion.vedibarta.chatSearch


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class SearchCharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharFragment@Search"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_characteristics, container, false)
        val act = (activity as ChatSearchActivity)
        val characteristics : Array<String> = if (VedibartaActivity.student!!.gender != Gender.FEMALE)
            resources.getStringArray(R.array.characteristicsMale)
        else
            resources.getStringArray(R.array.characteristicsFemale)
        val table = view.findViewById(R.id.searchCharacteristics) as TableLayout
        populateCharacteristicsTable(act, table, characteristics.toMutableList().shuffled().toTypedArray(), act.fakeStudent)

        return view
    }

}
