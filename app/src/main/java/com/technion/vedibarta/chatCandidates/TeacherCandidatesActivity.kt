package com.technion.vedibarta.chatCandidates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.navArgs
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.R
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.adapters.TeacherCandidatesAdapter
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.database.DatabaseVersioning
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

                val chat = Chat().create(other)
                DatabaseVersioning.currentVersion.instance
                    .collection("chats").document(chat.chat!!)
                    .set(chat).addOnSuccessListener {
                        val intent = Intent(this@TeacherCandidatesActivity, ChatRoomActivity::class.java)

                        val chatMetadata = ChatMetadata(
                            chat.chat!!,
                            other.uid,
                            chat.getName(other.uid),
                            chat.numMessages,
                            chat.lastMessage,
                            chat.lastMessageTimestamp,
                            other.gender,
                            other.photo
                        )

                        intent.putExtra("chatData", chatMetadata)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
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

        val teachers = args.teachers.toList()

        carousel.initialize(carouselAdapter)
        carousel.setViewsToChangeColor(listOf()) // TODO: change color of pushed-back cards
        carouselAdapter.setItems(teachers)
        carousel.smoothScrollToPosition(0)
    }
}
