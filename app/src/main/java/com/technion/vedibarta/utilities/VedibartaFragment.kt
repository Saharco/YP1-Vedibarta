package com.technion.vedibarta.utilities

import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R

open class VedibartaFragment : Fragment(){

    companion object{

        private const val SELECTED_BUBBLE = 1
        private const val NON_SELECTED_BUBBLE = 0

        fun populateAutoTextView(activity : VedibartaActivity, autoCompTV : AutoCompleteTextView, array: Array<String>){
            val adapter = ArrayAdapter(activity.applicationContext!!, android.R.layout.simple_dropdown_item_1line, array)
            autoCompTV.setAdapter(adapter)
        }

        //---Characteristics Functions---
        fun populateCharacteristicsTable(activity: VedibartaActivity, table: TableLayout, characteristics: Array<String>, student: Student){

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

            val steps = calculateBubblesInRow(activity)
            (characteristics.indices step steps).forEach { i ->
                val tableRow = TableRow(activity)
                tableRow.id = i
                tableRow.layoutParams = tableRowParams
                tableRow.gravity = Gravity.CENTER_HORIZONTAL

                var bubbleFrame: FrameLayout

                for (j in 0 until steps) {
                    if (i + j >= characteristics.size)
                        break
                    if (student.characteristics.contains(characteristics[i + j])) {
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
                    bubbleFrame.setOnClickListener { characteristicsTableItemClickHandler(it, activity, characteristics, table, student) }
                    val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                    bubble.text = characteristics[i + j]
                    bubbleFrame.layoutParams = bubbleParams
                    tableRow.addView(bubbleFrame)
                }

                table.addView(tableRow)
            }
        }

        private fun characteristicsTableItemClickHandler(view: View, activity: VedibartaActivity, characteristics: Array<String>, table: TableLayout, student: Student) {
            val steps = calculateBubblesInRow(activity)
            val row = view.id / steps
            val tableRow = table[row] as TableRow
            val bubbleFrame: FrameLayout
            val viewPos = view.id % steps


            if (tableRow[view.id % steps].tag == NON_SELECTED_BUBBLE) {
                bubbleFrame = LayoutInflater.from(activity).inflate(
                    R.layout.user_profile_bubble_blue,
                    null
                ) as FrameLayout

                bubbleFrame.alpha = 1f
                bubbleFrame.tag = SELECTED_BUBBLE


                student.characteristics[characteristics[view.id]] = true

            } else {
                bubbleFrame = LayoutInflater.from(activity).inflate(
                    R.layout.user_profile_bubble_blue_selected,
                    null
                ) as FrameLayout

                bubbleFrame.alpha = 0.6f
                bubbleFrame.tag = NON_SELECTED_BUBBLE


               student.characteristics.remove(characteristics[view.id])
            }

            bubbleFrame.id = view.id
            bubbleFrame.setOnClickListener { characteristicsTableItemClickHandler(it, activity, characteristics, table, student) }


            val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
            bubble.text = characteristics[view.id]
            bubbleFrame.layoutParams = tableRow[viewPos].layoutParams

            tableRow.removeViewAt(viewPos)
            tableRow.addView(bubbleFrame, viewPos)
        }
        //-------------------------------

        //---Hobbies Functions---

        fun populateHobbiesTable(activity: VedibartaActivity, table: TableLayout, hobbies: Array<String>, student: Student) {

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

            val steps = calculateBubblesInRow(activity)
            (hobbies.indices step steps).forEach { i ->
                val tableRow = TableRow(activity)
                tableRow.id = i
                tableRow.layoutParams = tableRowParams
                tableRow.gravity = Gravity.CENTER_HORIZONTAL

                var bubbleFrame: FrameLayout

                for (j in 0 until steps) {
                    if (i + j >= hobbies.size)
                        break
                    if (student.hobbies.contains(hobbies[i + j])) {
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
                    bubbleFrame.setOnClickListener { hobbiesItemClickHandler(it,activity,hobbies,table,student) }
                    val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                    bubble.text = hobbies[i + j]
                    bubbleFrame.layoutParams = bubbleParams
                    tableRow.addView(bubbleFrame)
                }

                table.addView(tableRow)
            }
        }

        private fun hobbiesItemClickHandler(view: View, activity: VedibartaActivity, hobbies: Array<String>, table: TableLayout, student: Student) {
            val steps = calculateBubblesInRow(activity)
            val row = view.id / steps
            val tableRow = table[row] as TableRow
            val bubbleFrame: FrameLayout
            val viewPos = view.id % steps


            if (tableRow[view.id % steps].tag == NON_SELECTED_BUBBLE) {
                bubbleFrame = LayoutInflater.from(activity).inflate(
                    R.layout.user_profile_bubble_orange,
                    null
                ) as FrameLayout

                bubbleFrame.alpha = 1f
                bubbleFrame.tag = SELECTED_BUBBLE


                student.hobbies = student.hobbies.plusElement(hobbies[view.id])

            } else {
                bubbleFrame = LayoutInflater.from(activity).inflate(
                    R.layout.user_profile_bubble_orange_selected,
                    null
                ) as FrameLayout

                bubbleFrame.alpha = 0.6f
                bubbleFrame.tag = NON_SELECTED_BUBBLE

                student.hobbies = student.hobbies.filter { element -> element != hobbies[view.id]}
            }

            bubbleFrame.id = view.id
            bubbleFrame.setOnClickListener { hobbiesItemClickHandler(it, activity, hobbies, table, student) }

            val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
            bubble.text = hobbies[view.id]
            bubbleFrame.layoutParams = tableRow[viewPos].layoutParams

            tableRow.removeViewAt(viewPos)
            tableRow.addView(bubbleFrame, viewPos)
        }
        //-----------------------
        //Marks a text view as a required field by adding a red * at the end of the text
        fun TextView.markRequired() {
            text = buildSpannedString {
                append(text)
                color(Color.RED) { append(" *") } // Mind the space prefix.
            }
        }

        //Calculate the number of bubbles that can fit in the table
        //The Function calculates according to current screen size
        private fun calculateBubblesInRow(activity: VedibartaActivity): Int =
            ((Resources.getSystem().displayMetrics.widthPixels - VedibartaActivity.dpToPx(
                activity.resources,
                48f
            )) / VedibartaActivity.dpToPx(
                activity.resources,
                100f
            )).toInt()
    }

    protected open fun setupAndInitViews(v: View) = Unit
}