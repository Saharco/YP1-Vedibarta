package com.technion.vedibarta.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity
import java.util.HashMap

internal class MainAdapter(private val userId: String?,
                          private val applicationContext: Context,
                          private val chatPartnersMap: HashMap<String, ChatMetadata>,
                          private val mainActivity: MainActivity,
                           options: FirestoreRecyclerOptions<Chat>): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    var chatsList: List<Chat> = listOf()
    val firestoreAdapter = getFireStoreAdapter(options,this)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder
    {
        val userNameView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
        return ViewHolder(userNameView, userId!!, applicationContext)
    }

    override fun getItemCount(): Int
    {
        return chatsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val chat = chatsList[position]
        when (holder) {
            is ViewHolder -> {
                DatabaseVersioning.currentVersion.instance.collection("students")
                    .document(chat.getPartnerId(VedibartaActivity.student!!.uid))
                    .get()
                    .addOnSuccessListener { otherStudent ->
                        Log.d(MainActivity.TAG, "$otherStudent\n has the following photo: ${otherStudent["photo"]}")
                        val otherStudentPhotoUrl = otherStudent["photo"] as String?
                        val otherGender = if (otherStudent["gender"] as String == "FEMALE")
                            Gender.FEMALE
                        else
                            Gender.MALE

                        holder.bind(chat, otherStudentPhotoUrl, otherGender)
                        val partnerId = chat.getPartnerId(userId!!)
                        val chatMetadata = ChatMetadata(
                            chat.chat!!,
                            partnerId,
                            chat.getName(partnerId),
                            chat.numMessages,
                            chat.lastMessage,
                            chat.lastMessageTimestamp,
                            otherGender,
                            otherStudentPhotoUrl
                        )

                        Log.d(MainActivity.TAG, "Binding chat with the following data: $chatMetadata")

                        chatPartnersMap[chatMetadata.partnerName] = chatMetadata

                        holder.view.setOnClickListener {
                            val i = Intent(mainActivity, ChatRoomActivity::class.java)
                            i.putExtra("chatData", chatMetadata)
                            mainActivity.startActivity(i)
                        }
                    }.addOnFailureListener {
                        holder.bind(chat)
                        val partnerId = chat.getPartnerId(userId!!)
                        val chatMetadata = ChatMetadata(
                            chat.chat!!,
                            partnerId,
                            chat.getName(partnerId),
                            chat.numMessages,
                            chat.lastMessage,
                            chat.lastMessageTimestamp
                        )

                        chatPartnersMap[chatMetadata.partnerName] = chatMetadata

                        holder.view.setOnClickListener {
                            val i = Intent(mainActivity, ChatRoomActivity::class.java)
                            i.putExtra("chatData", chatMetadata)
                            mainActivity.startActivity(i)
                        }
                    }
            }

        }
    }

    private fun getFireStoreAdapter(options: FirestoreRecyclerOptions<Chat>, mainAdapter: MainAdapter): FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder> {
        return object : FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>(options) {
            override fun onDataChanged()
            {
                super.onDataChanged()
                chatsList = this.snapshots.sortedByDescending { it.lastMessageTimestamp }
                mainAdapter.notifyDataSetChanged()
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return ViewHolder(userNameView, userId!!, applicationContext)
            } //implemented because it must return something, this value is never used

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                card: Chat
            ) {}//do nothing
        }
    }
}