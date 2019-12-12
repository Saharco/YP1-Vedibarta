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
import com.technion.vedibarta.POJOs.MessageType
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.activity_chat_room.view.*
import java.lang.Exception

class ChatRoomAdapter(private val chatRoomActivity: ChatRoomActivity,
                      options: FirestoreRecyclerOptions<Message>,
                      var numMessages: Int): FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options)
{
    private val MESSAGE_SOUND_INTERVAL: Long = 2000
    private val soundHandler = Handler()
    private val soundTask = Runnable { soundHandler.removeMessages(0) }
    private val chatView= chatRoomActivity.findViewById<RecyclerView>(R.id.chatView)

    override fun getItemViewType(position: Int): Int
    {
        return this.snapshots[position].messageType.ordinal
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
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is GeneratorMessageViewHolder -> holder.bind(message)
        }

        if (!soundHandler.hasMessages(0)) {
            soundHandler.removeCallbacksAndMessages(null)
            soundHandler.postDelayed(soundTask, MESSAGE_SOUND_INTERVAL)
            try {
                val res = tryToPlaySound(message.messageType, this.itemCount)
            } catch (e: Exception) {
                com.technion.vedibarta.utilities.error(e, "tryToPlaySound")
            }
        }
    }

    private fun tryToPlaySound(type: MessageType, count: Int): Boolean
    {
        if (count > numMessages) {
            // Update the amount of already-acknowledged messages
            numMessages = count
            // Let the handler know about the successful sound invocation, so it can lock it for a set time
            soundHandler.sendEmptyMessage(0)

            if (type == MessageType.USER)
            {
                val mp = MediaPlayer.create(chatRoomActivity, R.raw.message_sent_audio);
                mp.start();
                return true;
            } else {
                val mp = MediaPlayer.create(chatRoomActivity, R.raw.message_received_audio);
                mp.start();
                return true
            }
        }
        return false
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