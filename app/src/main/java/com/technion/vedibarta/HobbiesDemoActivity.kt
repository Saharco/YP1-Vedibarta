package com.technion.vedibarta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.adapters.HobbiesAdapter
import kotlinx.android.synthetic.main.activity_hobbies_demo.*

class HobbiesDemoActivity : AppCompatActivity() {

    val hobbyCards = listOf(
        HobbyCard("ספורט", listOf(Pair(1,"a"),Pair(1,"aa"),Pair(1,"aaa"),Pair(1,"aaaa"),Pair(1,"aaaaa"))),
        HobbyCard("אומנות", listOf(Pair(1,"bbb"),Pair(1,"bbbb"),Pair(1,"bbbbb"))),
        HobbyCard("בילויים", listOf(Pair(1,"c"),Pair(1,"cc"),Pair(1,"ccc"),Pair(1,"cccc"),Pair(1,"ccccc"))),
        HobbyCard("טכנלוגייה", listOf(Pair(1, "d"))),
        HobbyCard("משחקים", listOf()),
        HobbyCard("אחר", listOf())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies_demo)
        val fakeStudent = Student()
        hobbyTitlesList.adapter = HobbiesAdapter(hobbyCards, fakeStudent)
        hobbyTitlesList.layoutManager = LinearLayoutManager(this)
    }
}
