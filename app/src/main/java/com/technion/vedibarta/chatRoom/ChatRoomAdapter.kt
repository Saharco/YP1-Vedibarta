package com.technion.vedibarta.chatRoom

import android.media.MediaPlayer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.R

class ChatRoomAdapter(chatRoomActivity: ChatRoomActivity,
                      options: FirestoreRecyclerOptions<Message>,
                      numMessages: Int): FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options)
{
    private val soundPlayer = SoundPlayer(chatRoomActivity, numMessages)
    private val chatView= chatRoomActivity.findViewById<RecyclerView>(R.id.chatView)
    private val uid = chatRoomActivity.userId
    private val systemSender = chatRoomActivity.systemSender

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

    override fun getItemViewType(position: Int): Int
    {
        val sender = snapshots[position].sender
        if (sender == systemSender)
            return MessageType.SYSTEM.ordinal
        if (sender == uid)
            return MessageType.USER.ordinal
        return MessageType.OTHER.ordinal
    }

    override fun onDataChanged()
    {
        super.onDataChanged()
        val lastVisiblePosition = (chatView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
        if (this.itemCount - lastVisiblePosition <= 2)
            chatView.smoothScrollToPosition(this.itemCount)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        var view: View? = null
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
                    com.technion.vedibarta.R.layout.generator_message_holder,
                    parent,
                    false
                )
                return GeneratorMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        message: Message
    ) {
        var type = MessageType.USER
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
            itemView.findViewById<TextView>(R.id.sentMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.sentMessageTime).text = message.getTime()
        }
    }

    private class ReceivedMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(R.id.receivedMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.receivedMessageTime).text = message.getTime()
        }
    }
    private class GeneratorMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(R.id.generatorMessageBody).text = message.text
        }
    }
}