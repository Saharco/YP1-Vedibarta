package com.technion.vedibarta.chatCandidates

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CarouselAdapter
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.adapters.ItemViewHolder
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student
import kotlinx.android.synthetic.main.activity_chat_candidates.*
import java.sql.Timestamp

class ChatCandidatesActivity : VedibartaActivity() {

    private val TAG = "ChatCandidates"

    private val lambda : (Int, Student) -> Unit = { position: Int, _: Student ->
                carousel.smoothScrollToPosition(position) }

    private val carouselAdapter: CarouselAdapter = object : CarouselAdapter(this, lambda) {
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            holder.button.setOnClickListener {
                startActivity(Intent(this@ChatCandidatesActivity, ChatRoomActivity::class.java))
                finish()
            }
        }
    }

    //TODO: change these. they're here for testing
    private val possibleItems = listOf(
        student!!,
        Student(
            "יובל נהון",
            "https://firebasestorage.googleapis.com/v0/b/takecare-81dab.appspot.com/o/itemPictures%2FuserUploads%2F1hrI3pYVvrVbtnxzrRh8nH3A1Y92%2F1aa42704-181f-4698-91ec-166d093902ae?alt=media&token=0fef3b51-16f2-4918-b8ae-b5ff1dacf5eb",
            "נתניה",
            "אורט יד ליבוביץ'",
            Gender.MALE,
            Timestamp(System.currentTimeMillis()),
            listOf("מסורתי", "מזרחי", "צבר"),
            listOf("ספורט", "לגלוש", "רובוטיקה")
        ),
        Student(
            "ויקטור בניאס",
            "https://firebasestorage.googleapis.com/v0/b/takecare-81dab.appspot.com/o/itemPictures%2FuserUploads%2FjTj93R9w05WEVQoqLPLfU4BSjDl1%2Fc0f3aac9-08e1-437a-b9c3-4dd8217f0f16?alt=media&token=715bdec5-8a32-4b82-8f75-aa25bc179f4b",
            "טבריה",
            "עמל נופרים בגליל",
            Gender.MALE,
            Timestamp(System.currentTimeMillis()),
            listOf("רוסי", "חילוני", "עולה חדש", "מרכז", "זמני 1", "זמני 2", "זמני 3", "זמני 4", "זמני 5", "זמני 6", "זמני 7", "זמני 8"),
            listOf("ספורט", "לבשל", "בולונז")
        ),
        Student(
            "גיל צימרמן",
            null,
            "יקנעם",
            "טכניון",
            Gender.MALE,
            Timestamp(System.currentTimeMillis()),
            listOf("יהודי"),
            listOf()
        ),
        Student(
            "שרה כהן",
            null,
            "עמק חפר",
            "רמות ים",
            Gender.FEMALE,
            Timestamp(System.currentTimeMillis()),
            listOf(),
            listOf()
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_candidates)
        carousel.initialize(carouselAdapter)
        carousel.setViewsToChangeColor(listOf()) // TODO: change color of pushed-back cards
        carouselAdapter.setItems(getLargeListOfItems())
    }

    private fun getLargeListOfItems(): List<Student> {
        val items = mutableListOf<Student>()
        (0..40).map { items.add(possibleItems.random()) }
        return items
    }
}