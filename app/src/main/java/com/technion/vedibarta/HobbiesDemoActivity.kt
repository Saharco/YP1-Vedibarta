package com.technion.vedibarta

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_hobbies_demo.*

class HobbiesDemoActivity : VedibartaActivity() {

    val hobbyCards = listOf(
        HobbyCard("ספורט", arrayOf("a", "aa", "aaa", "aaaa", "aaaaa")),
        HobbyCard("אומנות", arrayOf("a", "aa", "aaa", "aaaa")),
        HobbyCard("בילויים", arrayOf("a", "aa")),
        HobbyCard("טכנלוגייה", arrayOf("a")),
        HobbyCard("משחקים", arrayOf()),
        HobbyCard("אחר", arrayOf())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies_demo)
        changeStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))

        val fakeStudent = Student()
        hobbyTitlesList.adapter = HobbiesAdapter(hobbyCards, fakeStudent)
        hobbyTitlesList.layoutManager = LinearLayoutManager(this)
    }
}
