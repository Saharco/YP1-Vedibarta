package com.technion.vedibarta.chatRoom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.R
import java.text.SimpleDateFormat
import java.util.*

/***
 * base class adapter for adapters which are used by the RecyclerView of message history
 * every adapter that is used in ChatRoomActivity should extend this
 */
abstract class ChatRoomAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    /***
     * starts adapter functionality such as updating UI, etc.
     */
    abstract fun startListening()

    /***
     * stops adapter functionality such as updating UI, etc.
     */
    abstract fun stopListening()

    abstract fun hasNoMessages(): Boolean

    abstract fun getFirstMessageOrNull(): Message?

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val view: View?
        when (viewType)
        {
            MessageType.USER.ordinal  ->
            {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.sent_message_holder, parent, false)
                return SentMessageViewHolder(view)
            }
            MessageType.OTHER.ordinal ->
            {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.received_message_holder, parent, false)
                return ReceivedMessageViewHolder(view)
            }
            else                      ->
            {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.generator_message_holder, parent, false)
                return GeneratorMessageViewHolder(view)
            }
        }
    }

    private fun dateToTime(date: Date): String = SimpleDateFormat("HH:mma", Locale.getDefault()).format(date)

    inner class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        fun bind(message: Message)
        {
            val dateString = message.timestamp?.let { dateToTime(it) }
                ?: itemView.context.getString(R.string.sending_message)
            itemView.findViewById<TextView>(R.id.sentMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.sentMessageTime).text = dateString
        }
    }

    inner class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        fun bind(message: Message)
        {
            val dateString = dateToTime(message.timestamp!!)
            itemView.findViewById<TextView>(R.id.receivedMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.receivedMessageTime).text = dateString
        }
    }

    inner class GeneratorMessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        fun bind(message: Message)
        {
            itemView.findViewById<TextView>(R.id.generatorMessageBody).text = message.text
        }
    }
}