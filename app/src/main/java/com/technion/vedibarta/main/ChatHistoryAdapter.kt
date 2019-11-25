package com.technion.vedibarta.main

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.chat_card.view.*
import java.util.*
import kotlin.collections.ArrayList

class ChatHistoryAdapter(private val chatCards: ArrayList<ChatCard>) : RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>() {

    class ViewHolder (val view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userNameView = LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
        return ViewHolder(userNameView)
    }

    override fun getItemCount() = chatCards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.user_name.text = chatCards[position].userName
        holder.view.last_message.text = chatCards[position].lastMessage
        holder.view.relative_timestamp.text = DateUtils.getRelativeDateTimeString(holder.view.context, Date().time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0)
    }

}
