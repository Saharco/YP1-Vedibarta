package com.technion.vedibarta.dagger

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomAdapter
import dagger.Module
import dagger.Provides
import java.util.*

@Module
class ChatInnerAdapterModule(private val outerAdapter: ChatRoomAdapter,
                             private val messageList: MutableList<Message>,
                             private val options: FirestoreRecyclerOptions<Message>)
{
    @Provides
    fun adapter(): FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    {
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
                    outerAdapter.notifyDataSetChanged()
                }
                else if (sizeDiff == 1)
                {
                    Log.d("wtf", "1")
                    messageList.add(0, newMessageList.first())
                    outerAdapter.notifyItemInserted(0)
                }
            }

            override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
            ): RecyclerView.ViewHolder {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return object: RecyclerView.ViewHolder(view)
                {
                    //implemented because it must return something, this value is never used
                }
            }
            override fun onBindViewHolder(
                    holder: RecyclerView.ViewHolder,
                    position: Int,
                    message: Message
            ) {}//do nothing
        }
    }
}