package com.technion.vedibarta.utilities

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import de.hdodenhof.circleimageview.CircleImageView
import com.bumptech.glide.request.RequestOptions
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.services.Languages
import com.technion.vedibarta.utilities.services.translate


open class VedibartaFragment : Fragment() {

    companion object {

        private const val SELECTED_BUBBLE = 1
        private const val NON_SELECTED_BUBBLE = 0

        fun populateAutoTextView(
            activity: VedibartaActivity,
            autoCompTV: AutoCompleteTextView,
            array: Array<String>
        ) {
            val adapter = ArrayAdapter(
                activity.applicationContext!!,
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
            student: Student
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

                var bubbleFrame: FrameLayout

                for (j in 0 until steps) {
                    if (i + j >= characteristics.size)
                        break
                    if (isContained(context, student, characteristics[i + j])) {
                        bubbleFrame = LayoutInflater.from(context).inflate(
                            R.layout.user_profile_bubble_blue,
                            null
                        ) as FrameLayout
                        bubbleFrame.alpha = 1f
                        bubbleFrame.tag = SELECTED_BUBBLE
                    } else {
                        bubbleFrame = LayoutInflater.from(context).inflate(
                            R.layout.user_profile_bubble_blue_selected,
                            null
                        ) as FrameLayout
                        bubbleFrame.alpha = 0.6f
                        bubbleFrame.tag = NON_SELECTED_BUBBLE
                    }
                    bubbleFrame.id = i + j
                    bubbleFrame.setOnClickListener {
                        characteristicsTableItemClickHandler(
                            it,
                            context,
                            characteristics,
                            table,
                            student
                        )
                    }
                    val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                    bubble.text = characteristics[i + j]
                    bubbleFrame.layoutParams = bubbleParams
                    tableRow.addView(bubbleFrame)
                }

                table.addView(tableRow)
            }
        }

        private fun isContained(
            context: Context,
            student: Student,
            s: String
        ): Boolean {

           if(student.gender == Gender.NONE)
               return false

            val characteristic: String = s.translate(context)
                .characteristics()
                .from(Languages.HEBREW, student.gender)
                .to(Languages.BASE)
                .execute().first()
            return student.characteristics.contains(characteristic)
        }

        private fun characteristicsTableItemClickHandler(
            view: View,
            context: Context,
            characteristics: Array<String>,
            table: TableLayout,
            student: Student
        ) {
            val steps = calculateBubblesInRow(context)
            val row = view.id / steps
            val tableRow = table[row] as TableRow
            val bubbleFrame: FrameLayout
            val viewPos = view.id % steps

            if (tableRow[view.id % steps].tag == NON_SELECTED_BUBBLE) {
                bubbleFrame = LayoutInflater.from(context).inflate(
                    R.layout.user_profile_bubble_blue,
                    null
                ) as FrameLayout

                bubbleFrame.alpha = 1f
                bubbleFrame.tag = SELECTED_BUBBLE

                val char = characteristics[view.id].translate(context).characteristics()
                    .from(Languages.HEBREW, student.gender)
                    .to(Languages.BASE).execute().first()

                student.characteristics[char] = true

            } else {
                bubbleFrame = LayoutInflater.from(context).inflate(
                    R.layout.user_profile_bubble_blue_selected,
                    null
                ) as FrameLayout

                bubbleFrame.alpha = 0.6f
                bubbleFrame.tag = NON_SELECTED_BUBBLE

                val char = characteristics[view.id].translate(context).characteristics()
                    .from(Languages.HEBREW, student.gender)
                    .to(Languages.BASE).execute().first()

                student.characteristics.remove(char)

            }

            bubbleFrame.id = view.id
            bubbleFrame.setOnClickListener {
                characteristicsTableItemClickHandler(
                    it,
                    context,
                    characteristics,
                    table,
                    student
                )
            }


            val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
            bubble.text = characteristics[view.id]
            bubbleFrame.layoutParams = tableRow[viewPos].layoutParams

            tableRow.removeViewAt(viewPos)
            tableRow.addView(bubbleFrame, viewPos)
        }
        //-------------------------------

        //---Hobbies Functions---

        fun populateHobbiesTable(
            context: Context,
            table: TableLayout,
            hobbies: Array<String>,
            student: Student
        ) {
            val allHobbies = context.resources.getStringArray(R.array.hobbies)

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
                        .load(hobbyToPhoto(hobbies[i + j], allHobbies))
                        .into(bubblePhoto)
                    val bubbleText = bubbleFrame.findViewById(R.id.hobbyText) as TextView

                    bubbleText.text = hobbies[i + j]
                    val hobby = hobbies[i + j].translate(context).hobbies()
                        .from(Languages.HEBREW)
                        .to(Languages.BASE)
                        .execute().first()
                    if (student.hobbies.contains(hobby)) {
                        bubblePhoto.alpha = 1f
                        bubbleFrame.tag = SELECTED_BUBBLE
//                        bubbleText.visibility = View.GONE
                    } else {
                        bubbleFrame.tag = NON_SELECTED_BUBBLE
//                        bubbleText.visibility = View.VISIBLE
                    }
                    bubbleFrame.id = i + j
                    bubbleFrame.setOnClickListener {
                        hobbiesItemClickHandler(
                            it,
                            context,
                            hobbies,
                            table,
                            student
                        )
                    }
                    bubbleFrame.layoutParams = bubbleParams
                    tableRow.addView(bubbleFrame)
                }

                table.addView(tableRow)
            }
        }

        private fun hobbyToPhoto(hobby: String, hobbies: Array<String>): Int {

            return when (hobby) {
                hobbies[0] -> R.drawable.music
                hobbies[1] -> R.drawable.theatre
                hobbies[2] -> R.drawable.dance
                hobbies[3] -> R.drawable.photography
                hobbies[4] -> R.drawable.drawing

                hobbies[5] -> R.drawable.soccer
                hobbies[6] -> R.drawable.basketball
                hobbies[7] -> R.drawable.tennis
                hobbies[8] -> R.drawable.swimming
                hobbies[9] -> R.drawable.hiking
                hobbies[10] -> R.drawable.bicycle
                hobbies[11] -> R.drawable.martial_arts

                hobbies[12] -> R.drawable.robotics
                hobbies[13] -> R.drawable.science
                hobbies[14] -> R.drawable.math
                hobbies[15] -> R.drawable.computers

                hobbies[16] -> R.drawable.movies
                hobbies[17] -> R.drawable.shopping
                hobbies[18] -> R.drawable.friends
                hobbies[19] -> R.drawable.food
                hobbies[20] -> R.drawable.tv_series
                hobbies[21] -> R.drawable.youth_organization
                hobbies[22] -> R.drawable.fashion

                hobbies[23] -> R.drawable.video_games
                hobbies[24] -> R.drawable.board_games

                hobbies[25] -> R.drawable.books
                hobbies[26] -> R.drawable.nature
                hobbies[27] -> R.drawable.politics
                hobbies[28] -> R.drawable.cooking

                else -> android.R.drawable.ic_menu_help
            }
        }

        fun loadHobbies(context: Context): List<HobbyCard> {
            val hobbiesLinkArray = context.resources.obtainTypedArray(R.array.hobbies_id_link)
            val categories = context.resources.getStringArray(R.array.hobbies_categories)
            // these are the final indexes of each category for all hobbies

            val hobbyCards = mutableListOf<HobbyCard>()

            categories.forEachIndexed { index, category ->
                val hobbyId = hobbiesLinkArray.getResourceId(index, -1)
                hobbyCards.add(
                    index,
                    HobbyCard(category, context.resources.getStringArray(hobbyId))
                )
            }

            hobbiesLinkArray.recycle()
            return hobbyCards
        }

        private fun hobbiesItemClickHandler(
            view: View,
            context: Context,
            hobbies: Array<String>,
            table: TableLayout,
            student: Student
        ) {
            val steps = calculateBubblesInRow(context)
            val row = view.id / steps
            val tableRow = table[row] as TableRow
            val bubbleFrame: ConstraintLayout = view as ConstraintLayout
            val bubblePhoto = bubbleFrame.findViewById(R.id.hobbyPhoto) as CircleImageView
            val bubbleText = bubbleFrame.findViewById(R.id.hobbyText) as TextView

            val hobby = hobbies[view.id].translate(context).hobbies()
                .from(Languages.HEBREW)
                .to(Languages.BASE)
                .execute().first()
            if (tableRow[view.id % steps].tag == NON_SELECTED_BUBBLE) {
                bubblePhoto.animate().alpha(1f).duration = 400
//                bubbleText.visibility = View.GONE
                bubbleFrame.tag = SELECTED_BUBBLE
                student.hobbies = student.hobbies.plusElement(hobby)

            } else {
                bubblePhoto.animate().alpha(0.4f).duration = 400
//                bubbleText.visibility = View.VISIBLE
                bubbleFrame.tag = NON_SELECTED_BUBBLE
                student.hobbies =
                    student.hobbies.filter { element -> element != hobby }
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
    }

    protected open fun setupAndInitViews(v: View) = Unit
}