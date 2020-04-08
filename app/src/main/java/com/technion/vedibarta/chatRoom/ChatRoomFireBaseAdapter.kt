package com.technion.vedibarta.chatRoom

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.R
import com.technion.vedibarta.dagger.ChatInnerAdapterModule
import com.technion.vedibarta.dagger.DaggerChatInnerAdapterInjector
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/***
 * adapter wrapping for the FireBaseAdapter to be used by the RecyclerView of ChatRoomActivity
 */
class ChatRoomFireBaseAdapter(
    options: FirestoreRecyclerOptions<Message>,
    private val userId: String,
    private val systemSenderId: String,
    private val soundPlayer: SoundPlayer
) : ChatRoomAdapter()
{
    private var messageList: MutableList<Message> = mutableListOf()
    @Inject lateinit var fireStoreAdapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>

    init
    {
        DaggerChatInnerAdapterInjector.builder()
                .chatInnerAdapterModule(ChatInnerAdapterModule(this,messageList, options))
                .build()
                .inject(this)
    }

    override fun getFirstMessageOrNull(): Message? {
        return messageList.firstOrNull()
    }

    override fun hasNoMessages() = messageList.isEmpty()

    override fun startListening()
    {
        fireStoreAdapter.startListening()
        soundPlayer.init()
    }

    override fun stopListening()
    {
        fireStoreAdapter.stopListening()
        soundPlayer.release()
    }

    override fun getItemViewType(position: Int): Int {
        val sender = messageList[position].sender
        if (sender == systemSenderId)
            return MessageType.SYSTEM.ordinal
        if (sender == userId)
            return MessageType.USER.ordinal
        return MessageType.OTHER.ordinal
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var type = MessageType.USER
        val message = messageList[position]
        when (holder) {
            is SentMessageViewHolder -> {
                holder.bind(message)
                type = MessageType.USER
            }
            is ReceivedMessageViewHolder -> {
                holder.bind(message)
                type = MessageType.OTHER
            }
            is GeneratorMessageViewHolder -> {
                holder.bind(message)
                type = MessageType.SYSTEM
            }
        }

        soundPlayer.playMessageSound(type, this.itemCount)
    }

    private fun getFireStoreAdapter(options: FirestoreRecyclerOptions<Message>, mainAdapter: ChatRoomFireBaseAdapter): FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder> {
        return object : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {
            override fun onDataChanged()
            {
                super.onDataChanged()
                val newMessageList = this.snapshots.sortedWith(
                        compareByDescending<Message, Date?>(nullsLast()) { it.timestamp }
                )

                val sizeDiff = newMessageList.size - messageList.size
                if((sizeDiff > 1) or (sizeDiff == 0))
                {
                    Log.d("wtf", ">")
                    messageList.clear()
                    messageList.addAll(newMessageList)
                    mainAdapter.notifyDataSetChanged()
                }
                else if (sizeDiff == 1)
                {
                    Log.d("wtf", "1")
                    messageList.add(0, newMessageList.first())
                    mainAdapter.notifyItemInserted(0)
                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return GeneratorMessageViewHolder(userNameView)
            } //implemented because it must return something, this value is never used
            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                message: Message
            ) {}//do nothing
        }
    }
}
