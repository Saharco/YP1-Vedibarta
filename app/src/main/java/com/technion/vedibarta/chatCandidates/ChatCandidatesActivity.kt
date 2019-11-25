package com.technion.vedibarta.chatCandidates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CarouselAdapter
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_candidates.*
import java.sql.Timestamp

class ChatCandidatesActivity : VedibartaActivity() {

    private val TAG = "ChatCandidates"

    private val carouselAdapter = CarouselAdapter(this) { position: Int, _: Student ->
        carousel.smoothScrollToPosition(position)
    }

    private val possibleItems = listOf(
        student!!,
        Student(
            "יובל נהון",
            "https://firebasestorage.googleapis.com/v0/b/takecare-81dab.appspot.com/o/itemPictures%2FuserUploads%2F1hrI3pYVvrVbtnxzrRh8nH3A1Y92%2F1aa42704-181f-4698-91ec-166d093902ae?alt=media&token=0fef3b51-16f2-4918-b8ae-b5ff1dacf5eb",
            "נתניה",
            "אורט יד ליבוביץ'",
            Gender.MALE,
            Timestamp(System.currentTimeMillis()),
            arrayOf("מסורתי", "מזרחי", "צבר"),
            arrayOf("ספורט", "לגלוש", "רובוטיקה")
        ),
        Student(
            "ויקטור בניאס",
            "https://firebasestorage.googleapis.com/v0/b/takecare-81dab.appspot.com/o/itemPictures%2FuserUploads%2FjTj93R9w05WEVQoqLPLfU4BSjDl1%2Fc0f3aac9-08e1-437a-b9c3-4dd8217f0f16?alt=media&token=715bdec5-8a32-4b82-8f75-aa25bc179f4b",
            "טבריה",
            "עמל נופרים בגליל",
            Gender.MALE,
            Timestamp(System.currentTimeMillis()),
            arrayOf("צבר", "רוסי", "חילוני", "עולה חדש"),
            arrayOf("ספורט", "לבשל", "בולונז")
        ),
        Student(
            "גיל צימרמן",
            null,
            "יקנעם",
            "טכניון",
            Gender.MALE,
            Timestamp(System.currentTimeMillis()),
            arrayOf("יהודי"),
            arrayOf()
        ),
        Student(
            "שרה כהן",
            null,
            "עמק חפר",
            "רמות ים",
            Gender.FEMALE,
            Timestamp(System.currentTimeMillis()),
            arrayOf(),
            arrayOf()
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
