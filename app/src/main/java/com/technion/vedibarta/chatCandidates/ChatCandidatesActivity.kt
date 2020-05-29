package com.technion.vedibarta.chatCandidates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.navArgs
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CarouselAdapter
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.adapters.ItemViewHolder
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_candidates.*

class ChatCandidatesActivity : VedibartaActivity() {

    companion object {
        const val TAG = "Vedibarta/candidates"
    }

    private val lambda: (Int, Student) -> Unit = { position: Int, _: Student ->
        carousel.smoothScrollToPosition(position)
    }
    val args: ChatCandidatesActivityArgs by navArgs()

    private val carouselAdapter: CarouselAdapter = object : CarouselAdapter(this, lambda) {
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)

            holder.button.setOnClickListener {
                val other = carouselAdapterItems[holder.adapterPosition]

                val chat = Chat().create(other)
                Log.d(TAG, "chat id is: ${chat.chat}")
                val docRef =
                    DatabaseVersioning.currentVersion.instance.collection("chats").document(chat.chat!!)
                docRef.set(chat)
                    .addOnSuccessListener {
                        val intent =
                            Intent(this@ChatCandidatesActivity, ChatRoomActivity::class.java)

                        val chatMetadata = ChatMetadata(
                            chat.chat!!,
                            other.uid,
                            chat.getName(other.uid),
                            chat.numMessages,
                            chat.lastMessage,
                            chat.lastMessageTimestamp,
                            other.gender,
                            other.photo,
                            other.hobbies.toTypedArray()
                        )

                        intent.putExtra("chatData", chatMetadata)
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

        val students = args.filteredStudents.toList()

        carousel.initialize(carouselAdapter)
        carousel.setViewsToChangeColor(listOf()) // TODO: change color of pushed-back cards
        carouselAdapter.setItems(students)
        carousel.smoothScrollToPosition(0)
    }
}
