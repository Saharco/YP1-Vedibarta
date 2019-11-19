package com.technion.vedibarta.userProfile

import android.annotation.SuppressLint
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_profile_edit.*
import kotlinx.android.synthetic.main.fragment_profile_edit_characteristics.*
import kotlinx.android.synthetic.main.fragment_profile_edit_hobbies.*

class ProfileEditActivity : VedibartaActivity() {

    private val TAG = "ProfileEditActivity"

    private val SELECTED_BUBBLE = 1
    private val NON_SELECTED_BUBBLE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        Log.d(TAG, "created ProfileEditActivity")
        initWidgets()
    }

    private fun initWidgets() {
        setToolbar(toolbar_edit_profile)
        loadUserData()
    }

    private fun loadUserData() {
        populateCharacteristicsTable()
        populateHobbiesTable()
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
            handleNoCharacteristics()
            return
        }

        (characteristics.indices step 3).forEach { i ->
            val tableRow = TableRow(this)
            tableRow.id = i
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            var bubbleFrame: FrameLayout

            for (j in 0 until 3) {
                if (i + j >= characteristics.size)
                    break
                if (student!!.characteristics.contains(characteristics[i + j])) {
                    bubbleFrame = LayoutInflater.from(this).inflate(
                        R.layout.user_profile_bubble_selected,
                        null
                    ) as FrameLayout
                    bubbleFrame.tag = SELECTED_BUBBLE
                } else {
                    bubbleFrame = LayoutInflater.from(this).inflate(
                        R.layout.user_profile_bubble,
                        null
                    ) as FrameLayout
                    bubbleFrame.tag = NON_SELECTED_BUBBLE
                }
                bubbleFrame.id = i + j
                bubbleFrame.setOnClickListener { characteristicsItemClickHandler(it) }
                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = characteristics[i + j]
                bubbleFrame.layoutParams = bubbleParams
                tableRow.addView(bubbleFrame)
            }

            characteristicsTable_edit_profile.addView(tableRow)
        }
    }

    private fun characteristicsItemClickHandler(view: View) {
        val row = view.id / 3
        val tableRow = characteristicsTable_edit_profile[row] as TableRow
        val bubbleFrame: FrameLayout
        val viewPos = view.id % 3

        Log.d(TAG, "row: $row, View: ${view.id}")

        if (tableRow[view.id % 3].tag == NON_SELECTED_BUBBLE) {
            bubbleFrame = LayoutInflater.from(this).inflate(
                R.layout.user_profile_bubble_selected,
                null
            ) as FrameLayout
            bubbleFrame.tag = SELECTED_BUBBLE
        } else {
            bubbleFrame = LayoutInflater.from(this).inflate(
                R.layout.user_profile_bubble,
                null
            ) as FrameLayout
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


    private fun handleNoCharacteristics() {
        //TODO: add some behavior for the scenario where the user has no characteristics
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

        if (student == null || hobbies.isEmpty()) {
            handleNoHobbies()
            return
        }

        (hobbies.indices step 3).forEach { i ->
            val tableRow = TableRow(this)
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL
            var bubbleFrame: FrameLayout

            for (j in 0 until 3) {
                if (i + j >= hobbies.size)
                    break
                if (student!!.hobbies.contains(hobbies[i + j])) {
                    bubbleFrame = LayoutInflater.from(this).inflate(
                        R.layout.user_profile_bubble_orange_selected,
                        null
                    ) as FrameLayout
                    bubbleFrame.tag = SELECTED_BUBBLE
                } else {
                    bubbleFrame = LayoutInflater.from(this).inflate(
                        R.layout.user_profile_bubble_orange,
                        null
                    ) as FrameLayout
                    bubbleFrame.tag = NON_SELECTED_BUBBLE
                }
                bubbleFrame.id = i + j
                bubbleFrame.setOnClickListener { hobbiesItemClickHandler(it) }

                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = hobbies[i + j]
                bubbleFrame.layoutParams = bubbleParams

                tableRow.addView(bubbleFrame)
            }

            hobbiesTable_edit_profile.addView(tableRow)
        }
    }

    private fun hobbiesItemClickHandler(view: View) {
        val row = view.id / 3
        val tableRow = hobbiesTable_edit_profile[row] as TableRow
        val bubbleFrame: FrameLayout
        val viewPos = view.id % 3

        Log.d(TAG, "row: $row, View: ${view.id}")

        if (tableRow[view.id % 3].tag == NON_SELECTED_BUBBLE) {
            bubbleFrame = LayoutInflater.from(this).inflate(
                R.layout.user_profile_bubble_orange_selected,
                null
            ) as FrameLayout
            bubbleFrame.tag = SELECTED_BUBBLE
        } else {
            bubbleFrame = LayoutInflater.from(this).inflate(
                R.layout.user_profile_bubble_orange,
                null
            ) as FrameLayout
            bubbleFrame.tag = NON_SELECTED_BUBBLE
        }

        bubbleFrame.id = view.id
        bubbleFrame.setOnClickListener { hobbiesItemClickHandler(it) }

        Log.d(TAG, "Copying Text ${hobbies[view.id]}, View Id: ${view.id}, Row: $row")

        val bubble = (bubbleFrame.findViewById(R.id.invisibleBubble) as TextView)
        bubble.text = hobbies[view.id]
        bubbleFrame.layoutParams = tableRow[viewPos].layoutParams

        tableRow.removeViewAt(viewPos)
        tableRow.addView(bubbleFrame, viewPos)

    }

    private fun handleNoHobbies() {
        //TODO: add some behavior for the scenario where the user has no characteristics
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected started")
        super.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    fun onClickNexrButton(view: View) {
        characteristicsTable_edit_profile.visibility = View.GONE
        hobbiesTable_edit_profile.visibility = View.VISIBLE
        toolbar_edit_profile_title.text = "בחירת תחביבים"
        view.visibility = View.GONE
        button2.visibility = View.VISIBLE
    }

    fun onClickNexrButton2(view: View) {
        characteristicsTable_edit_profile.visibility = View.VISIBLE
        hobbiesTable_edit_profile.visibility = View.GONE
        toolbar_edit_profile_title.text = resources.getString(R.string.profile_edit_characteristics_title)
        view.visibility = View.GONE
        button.visibility = View.VISIBLE
    }


}

