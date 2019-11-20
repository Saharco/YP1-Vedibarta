package com.technion.vedibarta.userProfile


import android.annotation.SuppressLint
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
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student

/**
 * A simple [Fragment] subclass.
 */
class ProfileEditCharacteristicsFragment : Fragment() {

    private val TAG = "CharFragment@Edit"

    private val SELECTED_BUBBLE = 1
    private val NON_SELECTED_BUBBLE = 0

    private lateinit var characteristics: Array<String>
    private lateinit var table: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_profile_edit_characteristics, container,
            false
        )
        characteristics = resources.getStringArray(R.array.characteristicsMale_hebrew)
        table = view.findViewById(R.id.editCharacteristicsTable) as TableLayout
        populateCharacteristicsTable()
        return view
    }

    @SuppressLint("InflateParams")
    private fun populateCharacteristicsTable() {

        val tableRowParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableRowParams.setMargins(40, 40, 40, 40)

        val bubbleParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (student == null || characteristics.isEmpty()) {
            return
        }

        (characteristics.indices step 3).forEach { i ->
            val tableRow = TableRow(activity)
            tableRow.id = i
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            var bubbleFrame: FrameLayout

            for (j in 0 until 3) {
                if (i + j >= characteristics.size)
                    break
                if (student!!.characteristics.contains(characteristics[i + j])) {
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
        val row = view.id / 3
        val tableRow = table[row] as TableRow
        val bubbleFrame: FrameLayout
        val viewPos = view.id % 3

        Log.d(TAG, "row: $row, View: ${view.id}")

        if (tableRow[view.id % 3].tag == NON_SELECTED_BUBBLE) {
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

        bubbleFrame.id = view.id
        bubbleFrame.setOnClickListener { characteristicsItemClickHandler(it) }

        Log.d(TAG, "Copying Text ${characteristics[view.id]}, View Id: ${view.id}, Row: $row")

        val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
        bubble.text = characteristics[view.id]
        bubbleFrame.layoutParams = tableRow[viewPos].layoutParams

        tableRow.removeViewAt(viewPos)
        tableRow.addView(bubbleFrame, viewPos)
    }
}
