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
import com.technion.vedibarta.utilities.VedibartaActivity

/**
 * A simple [Fragment] subclass.
 */
class ProfileEditHobbiesFragment : Fragment() {

    private val TAG = "HobbiesFragment@Edit"

    private val SELECTED_BUBBLE = 1
    private val NON_SELECTED_BUBBLE = 0

    private lateinit var hobbies: Array<String>
    private lateinit var table: TableLayout
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_edit_hobbies, container, false)
        hobbies = resources.getStringArray(R.array.hobbiesMale_hebrew)
        table = view.findViewById(R.id.editHobbiesTable) as TableLayout
        populateHobbiesTable()
        return view
    }

    @SuppressLint("InflateParams")
    private fun populateHobbiesTable() {

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

        if (VedibartaActivity.student == null || hobbies.isEmpty()) {
            return
        }

        (hobbies.indices step 3).forEach { i ->
            val tableRow = TableRow(activity)
            tableRow.id = i
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            var bubbleFrame: FrameLayout

            for (j in 0 until 3) {
                if (i + j >= hobbies.size)
                    break
                if (VedibartaActivity.student!!.hobbies.contains(hobbies[i + j])) {
                    bubbleFrame = LayoutInflater.from(activity).inflate(
                        R.layout.user_profile_bubble_orange,
                        null
                    ) as FrameLayout
                    bubbleFrame.alpha = 1f
                    bubbleFrame.tag = SELECTED_BUBBLE
                } else {
                    bubbleFrame = LayoutInflater.from(activity).inflate(
                        R.layout.user_profile_bubble_orange_selected,
                        null
                    ) as FrameLayout
                    bubbleFrame.alpha = 0.6f
                    bubbleFrame.tag = NON_SELECTED_BUBBLE
                }
                bubbleFrame.id = i + j
                bubbleFrame.setOnClickListener { hobbiesItemClickHandler(it) }
                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = hobbies[i + j]
                bubbleFrame.layoutParams = bubbleParams
                tableRow.addView(bubbleFrame)
            }

            table.addView(tableRow)
        }
    }

    private fun hobbiesItemClickHandler(view: View) {
        val row = view.id / 3
        val tableRow = table[row] as TableRow
        val bubbleFrame: FrameLayout
        val viewPos = view.id % 3

        Log.d(TAG, "row: $row, View: ${view.id}")

        if (tableRow[view.id % 3].tag == NON_SELECTED_BUBBLE) {
            bubbleFrame = LayoutInflater.from(activity).inflate(
                R.layout.user_profile_bubble_orange,
                null
            ) as FrameLayout
            bubbleFrame.alpha = 1f
            bubbleFrame.tag = SELECTED_BUBBLE
            Log.d(TAG, "Adding ${hobbies[view.id]} to the set" )
            (activity as ProfileEditActivity).editedHobbies.add(hobbies[view.id])
        } else {
            bubbleFrame = LayoutInflater.from(activity).inflate(
                R.layout.user_profile_bubble_orange_selected,
                null
            ) as FrameLayout
            bubbleFrame.alpha = 0.6f
            bubbleFrame.tag = NON_SELECTED_BUBBLE
            Log.d(TAG, "Removing ${hobbies[view.id]} from the set" )
            (activity as ProfileEditActivity).editedHobbies.remove(hobbies[view.id])
        }

        bubbleFrame.id = view.id
        bubbleFrame.setOnClickListener { hobbiesItemClickHandler(it) }

        val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
        bubble.text = hobbies[view.id]
        bubbleFrame.layoutParams = tableRow[viewPos].layoutParams

        tableRow.removeViewAt(viewPos)
        tableRow.addView(bubbleFrame, viewPos)
    }
}
