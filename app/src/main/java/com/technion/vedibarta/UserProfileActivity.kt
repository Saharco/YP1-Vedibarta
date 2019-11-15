package com.technion.vedibarta

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import kotlinx.android.synthetic.main.activity_user_profile.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout
import android.widget.TableLayout


class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        initWidgets()
    }

    private fun initWidgets() {
        initToolbar()
        populateTable()
    }

    private fun populateTable() {

        //TODO: when the data is fetched from the database, change this dummy list
        val characteristics = listOf(
            "זמני1", "זמני2", "זמני3", "זמני4", "זמני5", "זמני6", "זמני7", "זמני8",
            "זמני9", "זמני10", "זמני11", "זמני12", "זמני13", "זמני14", "זמני15", "זמני16"
        )
        val characteristicsAmount = characteristics.size

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

        for (i in 0 until characteristicsAmount step 3) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            for (j in 0 until 3) {
                if (i + j >= characteristicsAmount)
                    break

                val bubbleFrame = LayoutInflater.from(this).inflate(
                    R.layout.user_profile_bubble,
                    null
                ) as FrameLayout

                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = characteristics[i + j]
                bubbleFrame.layoutParams = bubbleParams

                tableRow.addView(bubbleFrame)
            }

            characteristicsTable.addView(tableRow)
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
}
