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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies_demo)
        changeStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))

        val hobbyCards = loadHobbies()

        val fakeStudent = Student()
        hobbyTitlesList.adapter = HobbiesAdapter(hobbyCards, fakeStudent)
        hobbyTitlesList.layoutManager = LinearLayoutManager(this)
    }

    private fun loadHobbies(): List<HobbyCard> {
        val hobbies = resources.getStringArray(R.array.hobbies)
        val categories = resources.getStringArray(R.array.hobbies_categories)

        // these are the final indexes of each category for all hobbies
        val categoryIndexes = arrayOf(4, 11, 15, 22, 24, 28)
        val hobbyCards = mutableListOf<HobbyCard>()
        var currCategory = 0
        var prevStartIndex = 0
        categoryIndexes.forEach { index ->
            val categoryHobbies = hobbies.slice(prevStartIndex..index).toTypedArray()
            hobbyCards.add(currCategory, HobbyCard(categories[currCategory], categoryHobbies))

            currCategory ++
            prevStartIndex = index + 1
        }

        return hobbyCards
    }
}
