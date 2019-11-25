package com.technion.vedibarta.chatCandidates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CarouselAdapter
import com.technion.vedibarta.adapters.CarouselAdapterItem
import kotlinx.android.synthetic.main.activity_chat_candidates.*

class ChatCandidatesActivity : AppCompatActivity() {

    private val TAG = "ChatCandidates"

    private val carouselAdapter = CarouselAdapter(this) { position: Int, _: CarouselAdapterItem ->
        carousel.smoothScrollToPosition(position)}

    private val possibleItems = listOf(
        CarouselAdapterItem("Camera", R.drawable.ic_camera_blue),
        CarouselAdapterItem("Upload", R.drawable.ic_upload_blue),
        CarouselAdapterItem("Gallery", R.drawable.ic_gallery_blue),
        CarouselAdapterItem("Check", R.drawable.ic_check_white),
        CarouselAdapterItem("Edit", R.drawable.ic_edit_white)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_candidates)
        carousel.initialize(carouselAdapter)
        carousel.setViewsToChangeColor(listOf()) // TODO: change color of pushed-back cards
        carouselAdapter.setItems(getLargeListOfItems())
    }

    private fun getLargeListOfItems(): List<CarouselAdapterItem> {
        val items = mutableListOf<CarouselAdapterItem>()
        (0..40).map { items.add(possibleItems.random()) }
        return items
    }
}
