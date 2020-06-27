package com.technion.vedibarta.chatCandidates

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.navArgs
import com.technion.vedibarta.R
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.adapters.TeacherCandidatesAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_candidates.*

class TeacherCandidatesActivity : VedibartaActivity() {

    companion object {
        const val TAG = "Vedibarta/candidates"
    }

    private val lambda: (Int, Teacher) -> Unit = { position: Int, _: Teacher ->
        carousel.smoothScrollToPosition(position)
    }

    private val args: TeacherCandidatesActivityArgs by navArgs()

    private val carouselAdapter = object : TeacherCandidatesAdapter(this@TeacherCandidatesActivity, lambda) {
        override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)

            holder.button.setOnClickListener {
                val other = carouselAdapterItems[holder.adapterPosition]

                Toast.makeText(
                    this@TeacherCandidatesActivity,
                    "Selected ${other.name} to chat with. G'day to ya!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_candidates)

        val teachers = args.teachers.toList()

        carousel.initialize(carouselAdapter)
        carousel.setViewsToChangeColor(listOf()) // TODO: change color of pushed-back cards
        carouselAdapter.setItems(teachers)
        carousel.smoothScrollToPosition(0)
    }
}
