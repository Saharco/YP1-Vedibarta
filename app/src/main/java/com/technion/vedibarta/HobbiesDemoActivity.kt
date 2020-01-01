package com.technion.vedibarta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.adapters.HobbiesAdapter
import kotlinx.android.synthetic.main.activity_hobbies_demo.*

class HobbiesDemoActivity : AppCompatActivity() {

    val hobbyCards = listOf(
        HobbyCard("ספורט", listOf()),
        HobbyCard("אומנות", listOf()),
        HobbyCard("בילויים", listOf()),
        HobbyCard("טכנלוגייה", listOf()),
        HobbyCard("משחקים", listOf()),
        HobbyCard("אחר", listOf())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies_demo)

        hobbyTitlesList.adapter = HobbiesAdapter(hobbyCards)
        hobbyTitlesList.layoutManager = LinearLayoutManager(this)
    }
}
