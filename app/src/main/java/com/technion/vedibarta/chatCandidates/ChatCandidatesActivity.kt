package com.technion.vedibarta.chatCandidates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CarouselAdapter
import com.technion.vedibarta.adapters.Item
import kotlinx.android.synthetic.main.activity_chat_candidates.*

class ChatCandidatesActivity : AppCompatActivity() {

    private val carouselAdapter =  CarouselAdapter { position: Int, _: Item ->
        carousel.smoothScrollToPosition(position)
    }

    private val possibleItems = listOf(
        Item("Camera", R.drawable.ic_camera_blue),
        Item("Upload", R.drawable.ic_upload_blue),
        Item("Gallery", R.drawable.ic_gallery_blue),
        Item("Check", R.drawable.ic_check_white),
        Item("Edit", R.drawable.ic_edit_white)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_candidates)
        carousel.initialize(carouselAdapter)
        carousel.setViewsToChangeColor(listOf()) // TODO: change color of pushed-back cards
        carouselAdapter.setItems(getLargeListOfItems())
    }

    private fun getLargeListOfItems(): List<Item> {
        val items = mutableListOf<Item>()
        (0..40).map { items.add(possibleItems.random()) }
        return items
    }
}
