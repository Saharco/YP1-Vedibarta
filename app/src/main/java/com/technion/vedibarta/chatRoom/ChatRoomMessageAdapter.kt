package com.technion.vedibarta.chatRoom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.POJOs.MessageType
import com.technion.vedibarta.R



//class ChatRoomMessageAdapter(
//    private val messages: ArrayList<Message>,
//    private val itemClick: (Message) -> Unit
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    override fun getItemCount() = messages.size
//
//    override fun getItemViewType(position: Int): Int {
//        return messages[position].messageType?.ordinal ?: 0
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        var view: View? = null
//        when (viewType) {
//            MessageType.USER.ordinal -> {
//                view = LayoutInflater.from(parent.context).inflate(
//                    R.layout.sent_message_holder,
//                    parent,
//                    false
//                )
//                return SentMessageViewHolder(view, itemClick)
//            }
//            MessageType.OTHER.ordinal -> {
//                view = LayoutInflater.from(parent.context).inflate(
//                    R.layout.received_message_holder,
//                    parent,
//                    false
//                )
//                return ReceivedMessageViewHolder(view, itemClick)
//            }
//            else -> {
//                view = LayoutInflater.from(parent.context).inflate(
//                    R.layout.generator_message_holder,
//                    parent,
//                    false
//                )
//                return GeneratorMessageViewHolder(view, itemClick)
//            }
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val message = messages[position]
//        when (holder) {
//            is SentMessageViewHolder -> holder.bind(message)
//            is ReceivedMessageViewHolder -> holder.bind(message)
//            is GeneratorMessageViewHolder -> holder.bind(message)
//        }
//    }
//
//    private class SentMessageViewHolder(view: View, val itemClick: (Message) -> Unit) :
//        RecyclerView.ViewHolder(view) {
//
//        fun bind(message: Message) {
//            itemView.findViewById<TextView>(R.id.sentMessageBody).text = message.text
//            itemView.findViewById<TextView>(R.id.sentMessageTime).text = message.getTime()
//        }
//    }
//
//    private class ReceivedMessageViewHolder(view: View, val itemClick: (Message) -> Unit) :
//        RecyclerView.ViewHolder(view) {
//
//        fun bind(message: Message) {
//            itemView.findViewById<TextView>(R.id.receivedMessageBody).text = message.text
//            itemView.findViewById<TextView>(R.id.receivedMessageTime).text = message.getTime()
//        }
//    }
//    private class GeneratorMessageViewHolder(view: View, val itemClick: (Message) -> Unit) :
//        RecyclerView.ViewHolder(view) {
//
//        fun bind(message: Message) {
//            itemView.findViewById<TextView>(R.id.generatorMessageBody).text = message.text
//        }
//    }
//}