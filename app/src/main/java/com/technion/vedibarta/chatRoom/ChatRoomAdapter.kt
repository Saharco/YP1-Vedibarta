package com.technion.vedibarta.chatRoom

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.activity_chat_room.*
import java.text.SimpleDateFormat
import java.util.*

class ChatRoomAdapter(
    chatRoomActivity: ChatRoomActivity,
    options: FirestoreRecyclerOptions<Message>,
    numMessages: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private val soundPlayer = SoundPlayer(chatRoomActivity, numMessages)
    private val uid = chatRoomActivity.userId
    private val systemSender = chatRoomActivity.systemSender
    private var messageList: List<Message> = listOf()
    val fireStoreAdapter = getFireStoreAdapter(options, this)

    fun getFirstMessageOrNull(): Message?
    {
        return messageList.firstOrNull()
    }

    override fun getItemViewType(position: Int): Int {
        val sender = messageList[position].sender
        if (sender == systemSender)
            return MessageType.SYSTEM.ordinal
        if (sender == uid)
            return MessageType.USER.ordinal
        return MessageType.OTHER.ordinal
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder
    {
        val view: View?
        when (viewType) {
            MessageType.USER.ordinal -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.sent_message_holder,
                    parent,
                    false
                )
                return SentMessageViewHolder(view)
            }
            MessageType.OTHER.ordinal -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.received_message_holder,
                    parent,
                    false
                )
                return ReceivedMessageViewHolder(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.generator_message_holder,
                    parent,
                    false
                )
                return GeneratorMessageViewHolder(view)
            }
        }
    }

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

    private class SentMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            val dateString = dateToString(message.timestamp ?: Date())
            itemView.findViewById<TextView>(R.id.sentMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.sentMessageTime).text = dateString
        }
    }

    private class ReceivedMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            val dateString = dateToString(message.timestamp!!)
            itemView.findViewById<TextView>(R.id.receivedMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.receivedMessageTime).text = dateString
        }
    }

    private class GeneratorMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(R.id.generatorMessageBody).text = message.text
        }
    }

    private fun getFireStoreAdapter(options: FirestoreRecyclerOptions<Message>, chatRoomAdapter: ChatRoomAdapter): FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder> {
        return object : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {
            override fun onDataChanged()
            {
                super.onDataChanged()
                messageList = this.snapshots.sortedWith(
                    compareByDescending<Message, Date?>(nullsLast()) { it.timestamp }
                )
                chatRoomAdapter.notifyDataSetChanged()
            }

            override fun startListening()
            {
                super.startListening()
                soundPlayer.init()
            }

            override fun stopListening()
            {
                super.stopListening()
                soundPlayer.release()
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

fun dateToString(date: Date): String = SimpleDateFormat("HH:mma").format(date)