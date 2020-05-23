package com.technion.vedibarta.utilities

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.technion.vedibarta.R
import de.hdodenhof.circleimageview.CircleImageView
import com.bumptech.glide.request.RequestOptions
import com.technion.vedibarta.utilities.resourcesManagement.*
import java.io.File


open class VedibartaFragment : Fragment() {

    companion object {

        private const val SELECTED_BUBBLE = 1
        private const val NON_SELECTED_BUBBLE = 0

        fun populateAutoTextView(
            context: Context,
            autoCompTV: AutoCompleteTextView,
            array: Array<String>
        ) {
            val adapter = ArrayAdapter(
                context.applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                array
            )
            autoCompTV.setAdapter(adapter)
        }

        //---Characteristics Functions---
        fun populateCharacteristicsTable(
            context: Context,
            table: TableLayout,
            characteristics: Array<String>,
            studentCharacteristics: MutableList<String>,
            characteristicsResource: MultilingualTextResource
        ) {

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

            val steps = calculateBubblesInRow(context)
            (characteristics.indices step steps).forEach { i ->
                val tableRow = TableRow(context)
                tableRow.id = i
                tableRow.layoutParams = tableRowParams
                tableRow.gravity = Gravity.CENTER_HORIZONTAL



                for (j in 0 until steps) {
                    if (i + j >= characteristics.size)
                        break
                    val bubbleFrame = LayoutInflater.from(context).inflate(R.layout.characteristic_bubble,
                        null
                    ) as ConstraintLayout
                    val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                    val text = bubbleFrame.findViewById(R.id.characteristicText) as TextView

                    if (isContained(
                            studentCharacteristics,
                            characteristics[i + j],
                            characteristicsResource
                        )
                    ) {
                        bubble.alpha = 1f
                        bubbleFrame.tag = SELECTED_BUBBLE
                        text.setTextColor(ContextCompat.getColor(context, R.color.textPrimaryOnDarkSurface))
                    } else {
                        bubble.alpha = 0.3f
                        bubbleFrame.tag = NON_SELECTED_BUBBLE
                        text.setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
                    }
                    bubbleFrame.id = i + j
                    bubbleFrame.setOnClickListener {
                        characteristicsTableItemClickHandler(
                            it,
                            context,
                            characteristics,
                            table,
                            studentCharacteristics,
                            characteristicsResource
                        )
                    }
                    text.text = characteristics[i + j]
                    bubbleFrame.layoutParams = bubbleParams
                    tableRow.addView(bubbleFrame)
                }

                table.addView(tableRow)
            }
        }

        private fun isContained(
            studentCharacteristics: MutableList<String>,
            s: String,
            characteristics: MultilingualTextResource
        ): Boolean {
            val characteristic = characteristics.toBaseLanguage(s)
            return studentCharacteristics.contains(characteristic)
        }

        fun characteristicsTableItemClickHandler(
            view: View,
            context: Context,
            characteristics: Array<String>,
            table: TableLayout,
            studentCharacteristics: MutableList<String>,
            characteristicsResource: MultilingualTextResource
        ) {
            val steps = calculateBubblesInRow(context)
            val row = view.id / steps
            val tableRow = table[row] as TableRow
            val bubbleFrame = view as ConstraintLayout
            val viewPos = view.id % steps
            val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
            val text = bubbleFrame.findViewById(R.id.characteristicText) as TextView
            val char = characteristicsResource.toBaseLanguage(characteristics[view.id])

            if (tableRow[view.id % steps].tag == NON_SELECTED_BUBBLE) {
                bubble.alpha = 1f
                bubbleFrame.tag = SELECTED_BUBBLE
                text.setTextColor(ContextCompat.getColor(context, R.color.textPrimaryOnDarkSurface))
                studentCharacteristics.add(char)

            } else {
                bubble.alpha = 0.3f
                bubbleFrame.tag = NON_SELECTED_BUBBLE
                studentCharacteristics.remove(char)
                text.setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
            }
        }
        //-------------------------------

        //---Hobbies Functions---

        fun populateHobbiesTable(
            context: Context,
            table: TableLayout,
            hobbies: Array<String>,
            studentHobbies: MutableList<String>,
            hobbiesResource: MultilingualTextResource,
            hobbiesPhotos: Map<String, File>
        ) {
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

            val steps = calculateBubblesInRow(context)
            (hobbies.indices step steps).forEach { i ->
                val tableRow = TableRow(context)
                tableRow.id = i
                tableRow.layoutParams = tableRowParams
                tableRow.gravity = Gravity.CENTER_HORIZONTAL


                for (j in 0 until steps) {
                    if (i + j >= hobbies.size)
                        break

                    val bubbleFrame = LayoutInflater.from(context).inflate(
                        R.layout.user_profile_bubble_hobby,
                        null
                    ) as ConstraintLayout
                    val bubblePhoto = bubbleFrame.findViewById(R.id.hobbyPhoto) as CircleImageView
                    val myOptions = RequestOptions().override(100, 100)
                    Glide.with(context)
                        .asBitmap()
                        .apply(myOptions)
                        .load(hobbiesPhotos["${hobbiesResource.toBaseLanguage(hobbies[i + j])}.jpg"])
                        .into(bubblePhoto)
                    val bubbleText = bubbleFrame.findViewById(R.id.hobbyText) as TextView

                    bubbleText.text = hobbies[i + j]
                    val hobby = hobbiesResource.toBaseLanguage(hobbies[i + j])

                    if (studentHobbies.contains(hobby)) {
                        bubblePhoto.alpha = 1f
                        bubbleFrame.tag = SELECTED_BUBBLE
                    } else {
                        bubbleFrame.tag = NON_SELECTED_BUBBLE
                    }
                    bubbleFrame.id = i + j
                    bubbleFrame.setOnClickListener {
                        hobbiesItemClickHandler(
                            it,
                            context,
                            hobbies,
                            table,
                            studentHobbies,
                            hobbiesResource
                        )
                    }
                    bubbleFrame.layoutParams = bubbleParams
                    tableRow.addView(bubbleFrame)
                }

                table.addView(tableRow)
            }
        }

        private fun hobbiesItemClickHandler(
            view: View,
            context: Context,
            hobbies: Array<String>,
            table: TableLayout,
            studentHobbies: MutableList<String>,
            hobbiesResource: MultilingualTextResource
        ) {
            val steps = calculateBubblesInRow(context)
            val row = view.id / steps
            val tableRow = table[row] as TableRow
            val bubbleFrame: ConstraintLayout = view as ConstraintLayout
            val bubblePhoto = bubbleFrame.findViewById(R.id.hobbyPhoto) as CircleImageView

            val hobby = hobbiesResource.toBaseLanguage(hobbies[view.id])
            if (tableRow[view.id % steps].tag == NON_SELECTED_BUBBLE) {
                bubblePhoto.animate().alpha(1f).duration = 400
                bubbleFrame.tag = SELECTED_BUBBLE
                studentHobbies.add(hobby)

            } else {
                bubblePhoto.animate().alpha(0.4f).duration = 400
                bubbleFrame.tag = NON_SELECTED_BUBBLE
                studentHobbies.remove(hobby)
            }

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
        private fun calculateBubblesInRow(context: Context): Int =
            ((Resources.getSystem().displayMetrics.widthPixels - VedibartaActivity.dpToPx(
                context.resources,
                48f
            )) / VedibartaActivity.dpToPx(
                context.resources,
                100f
            )).toInt()

        fun calculateBubblesInCol(context: Context): Int =
            ((Resources.getSystem().displayMetrics.heightPixels - VedibartaActivity.dpToPx(
                context.resources,
                48f
            )) / VedibartaActivity.dpToPx(
                context.resources,
                100f
            )).toInt()
    }

    protected open fun setupAndInitViews(v: View) = Unit

    interface ArgumentTransfer {
        fun getArgs(): Map<String, Any>
    }
}