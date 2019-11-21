package com.technion.vedibarta.chatSearch


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Spinner
import android.widget.Switch
import androidx.core.graphics.drawable.toDrawable

import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_search_extra_options.*

/**
 * A simple [Fragment] subclass.
 */
class SearchExtraOptionsFragment : Fragment() {

    private val TAG = "ExtraFragment@Search"

    lateinit var schoolsName: Array<String>
    lateinit var zonesName: Array<String>

    lateinit var schoolSwitch: Switch
    lateinit var zoneSwitch: Switch

    lateinit var schoolSpinner: Spinner
    lateinit var zoneSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_extra_options, container, false)
        schoolsName = resources.getStringArray(R.array.schoolNameList)
        zonesName = resources.getStringArray(R.array.zoneNameList)

        schoolSwitch = view.findViewById(R.id.schoolFilterSwitch)
        zoneSwitch = view.findViewById(R.id.zoneFilterSwitch)

        schoolSpinner = view.findViewById(R.id.schoolListSpinner)
        zoneSpinner = view.findViewById(R.id.zoneListSpinner)

        schoolSwitch.setOnCheckedChangeListener { _, isChecked -> schoolOnCheckedChanged(isChecked) }
        zoneSwitch.setOnCheckedChangeListener { _, isChecked -> zoneOnCheckedChanged(isChecked) }

        populateSpinners()

        return view
    }

    private fun populateSpinners() {

        Log.d(TAG, "${activity!!.applicationContext}")

        ArrayAdapter.createFromResource(
            this.context!!,
            R.array.schoolNameList,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            schoolSpinner.adapter = adapter

        }

        ArrayAdapter.createFromResource(
            this.context!!,
            R.array.zoneNameList,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            zoneSpinner.adapter = adapter
        }
    }

    private fun schoolOnCheckedChanged(isChecked: Boolean) {
        if (isChecked) {
            schoolListSpinner.visibility = View.VISIBLE
        } else {
            schoolListSpinner.visibility = View.GONE
        }
    }

    private fun zoneOnCheckedChanged(isChecked: Boolean) {

        if (isChecked) {
            zoneListSpinner.visibility = View.VISIBLE
        } else {
            zoneListSpinner.visibility = View.GONE
        }

    }

}
