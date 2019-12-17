package com.technion.vedibarta.chatCandidates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.vedibarta.ExtentionFunctions.create
import com.technion.vedibarta.ExtentionFunctions.getName
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CarouselAdapter
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.adapters.ItemViewHolder
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_candidates.*

class ChatCandidatesActivity : VedibartaActivity() {

    companion object {
        const val TAG = "Vedibarta/candidates"
    }

    private val lambda: (Int, Student) -> Unit = { position: Int, _: Student ->
        carousel.smoothScrollToPosition(position)
    }

    private val carouselAdapter: CarouselAdapter = object : CarouselAdapter(this, lambda) {
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)

            holder.button.setOnClickListener {
                val other = carouselAdapterItems[holder.adapterPosition]

                val chat = Chat().create(other)
                Log.d(TAG, "chat id is: ${chat.chat}")
                val docRef =
                    FirebaseFirestore.getInstance().collection("chats").document(chat.chat!!)
                docRef.set(chat)
                    .addOnSuccessListener {
                        val intent =
                            Intent(this@ChatCandidatesActivity, ChatRoomActivity::class.java)
                        intent.putExtra("chatId", chat.chat)
                        intent.putExtra("partnerId", other.uid)
                        intent.putExtra("name", chat.getName(other.uid))
                        intent.putExtra("photoUrl", other.photo)
                        intent.putExtra("otherGender", other.gender)
                        intent.putExtra("numMessages", chat.numMessages)

                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Log.d(TAG, "failed to create chat document")
                        Toast.makeText(
                            applicationContext,
                            "Failed to open chat",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_candidates)

        val students = intent.getParcelableArrayExtra("STUDENTS")!!.map { it as Student }

        carousel.initialize(carouselAdapter)
        carousel.setViewsToChangeColor(listOf()) // TODO: change color of pushed-back cards
        carouselAdapter.setItems(students)
    }
}
