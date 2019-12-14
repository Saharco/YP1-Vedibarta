package com.technion.vedibarta.chatCandidates

import android.content.Intent
import android.os.Bundle
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CarouselAdapter
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.adapters.ItemViewHolder
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_candidates.*

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
