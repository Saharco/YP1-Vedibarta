package com.technion.vedibarta.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap

/***
 * filtering adapter which filters by the name of the chat partner
 */
class MainSearchByNameAdapter(private val applicationContext: Context,
                              private val chatPartnersMap: HashMap<String, ArrayList<ChatMetadata>>,
                              private val mainActivity: MainActivity): MainsSearchAdapter<String>(mainActivity.chat_history)
{
    private var filteredList = listOf<ChatMetadata>()

    override fun filter(query: String)
    {
        filteredList = chatPartnersMap.filterKeys {
            it.startsWith(query, ignoreCase = true) or it.split(" ")[1].startsWith(query,
                                                                                   ignoreCase = true)
        }.values.flatten()
            .sortedByDescending { it.lastMessageTimestamp } // observe that that Date implements Comparable!

        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val userNameView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
        return ViewHolder(userNameView, mainActivity.userId!!, applicationContext)
    }

    override fun getItemCount(): Int
    {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        when(holder)
        {
            is ViewHolder -> {
                val chatMetadata = filteredList[holder.adapterPosition]
                Log.d(MainActivity.TAG, "Binding chat with the following data: $chatMetadata")
                holder.bind(chatMetadata)
                holder.view.setOnClickListener {
                    val intent = Intent(mainActivity, ChatRoomActivity::class.java)
                    intent.putExtra("chatData", chatMetadata)
                    mainActivity.startActivity(intent)
                }
            }
        }
    }
}