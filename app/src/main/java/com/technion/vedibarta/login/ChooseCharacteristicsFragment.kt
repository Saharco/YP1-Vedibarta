package com.technion.vedibarta.login


import android.content.res.Configuration
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
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student

/**
 * A simple [Fragment] subclass.
 */
class ChooseCharacteristicsFragment : Fragment() {

    private val TAG = "CharFragment@Setup"

    private val SELECTED_BUBBLE = 1
    private val NON_SELECTED_BUBBLE = 0

    private lateinit var characteristics: Array<String>
    private lateinit var table: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_characteristics, container, false)

        characteristics = resources.getStringArray(R.array.characteristicsMale_hebrew)
        table = view.findViewById(R.id.searchCharacteristics) as TableLayout
        populateCharacteristicsTable()


        return view
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed")
        populateCharacteristicsTable()
    }

    private fun populateCharacteristicsTable() {
        val act = (activity as UserSetupActivity)

        val tableRowParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableRowParams.topMargin = 40 // in pixels

        val bubbleParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (characteristics.isEmpty()) {
            return
        }

        val steps = calculateBubblesInRow()
        (characteristics.indices step steps).forEach { i ->
            val tableRow = TableRow(activity)
            tableRow.id = i
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            var bubbleFrame: FrameLayout

            for (j in 0 until steps) {
                if (i + j >= characteristics.size)
                    break
                if (act.setupStudent.characteristics.contains(characteristics[i + j])) {
                    bubbleFrame = LayoutInflater.from(activity).inflate(
                        R.layout.user_profile_bubble_blue,
                        null
                    ) as FrameLayout
                    bubbleFrame.alpha = 1f
                    bubbleFrame.tag = SELECTED_BUBBLE
                } else {
                    bubbleFrame = LayoutInflater.from(activity).inflate(
                        R.layout.user_profile_bubble_blue_selected,
                        null
                    ) as FrameLayout
                    bubbleFrame.alpha = 0.6f
                    bubbleFrame.tag = NON_SELECTED_BUBBLE
                }
                bubbleFrame.id = i + j
                bubbleFrame.setOnClickListener { characteristicsItemClickHandler(it) }
                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = characteristics[i + j]
                bubbleFrame.layoutParams = bubbleParams
                tableRow.addView(bubbleFrame)
            }

            table.addView(tableRow)
        }
    }

    private fun characteristicsItemClickHandler(view: View) {
        val steps = calculateBubblesInRow()
        val row = view.id / steps
        val tableRow = table[row] as TableRow
        val bubbleFrame: FrameLayout
        val viewPos = view.id % steps
        val act = (activity as UserSetupActivity)

        Log.d(TAG, "row: $row, View: ${view.id}")

        if (tableRow[view.id % steps].tag == NON_SELECTED_BUBBLE) {
            bubbleFrame = LayoutInflater.from(activity).inflate(
                R.layout.user_profile_bubble_blue,
                null
            ) as FrameLayout

            bubbleFrame.alpha = 1f
            bubbleFrame.tag = SELECTED_BUBBLE

            Log.d(TAG, "Adding ${characteristics[view.id]} to the set")

            act.setupStudent.characteristics =
                act.setupStudent.characteristics.plusElement(characteristics[view.id])

        } else {
            bubbleFrame = LayoutInflater.from(activity).inflate(
                R.layout.user_profile_bubble_blue_selected,
                null
            ) as FrameLayout

            bubbleFrame.alpha = 0.6f
            bubbleFrame.tag = NON_SELECTED_BUBBLE

            Log.d(TAG, "Removing ${characteristics[view.id]} from the set")

            act.setupStudent.characteristics =
                act.setupStudent.characteristics.filter { element -> element != characteristics[view.id] }
                    .toTypedArray()
        }

        bubbleFrame.id = view.id
        bubbleFrame.setOnClickListener { characteristicsItemClickHandler(it) }

        Log.d(TAG, "Copying Text ${characteristics[view.id]}, View Id: ${view.id}, Row: $row")

        val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
        bubble.text = characteristics[view.id]
        bubbleFrame.layoutParams = tableRow[viewPos].layoutParams

        tableRow.removeViewAt(viewPos)
        tableRow.addView(bubbleFrame, viewPos)
    }

    private fun calculateBubblesInRow(): Int =
        ((Resources.getSystem().displayMetrics.widthPixels - VedibartaActivity.dpToPx(
            resources,
            48f
        )) / VedibartaActivity.dpToPx(
            resources,
            100f
        )).toInt()


}
