package com.technion.vedibarta.chatSearch


import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.technion.vedibarta.POJOs.Student
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

        val characteristics = resources.getStringArray(R.array.characteristicsMale_hebrew)
        val table = view.findViewById(R.id.searchCharacteristics) as TableLayout

        populateCharacteristicsTable(act, table, characteristics, act.fakeStudent)

        return view
    }

}
