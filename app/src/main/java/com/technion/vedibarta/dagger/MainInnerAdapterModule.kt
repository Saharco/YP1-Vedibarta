package com.technion.vedibarta.dagger

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomAdapter
import com.technion.vedibarta.main.MainFireBaseAdapter
import com.technion.vedibarta.main.ViewHolder
import dagger.Module
import dagger.Provides

@Module
class MainInnerAdapterModule(private val outerAdapter: MainFireBaseAdapter,
                             private val chatsList: MutableList<Chat>,
                             private val options: FirestoreRecyclerOptions<Chat>)
{
    @Provides
    fun adapter(): FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>
    {
        return object : FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>(options)
        {
            // onDataChange should be called on every change and as such there shouldn't be more
            // then 1 change at a time to the list except for first initialization
            override fun onDataChanged()
            {
                super.onDataChanged()

                val newList = this.snapshots.sortedByDescending { it.lastMessageTimestamp }
                when
                {
                    (newList.size - chatsList.size) == 1 ->
                    {
                        chatsList.add(0, newList.first())
                        outerAdapter.notifyItemInserted(0)
                    }
                    (newList.size - chatsList.size) > 1  ->
                    {
                        // first initialization of list
                        chatsList.addAll(newList)
                        outerAdapter.notifyItemInserted(0)
                    }
                    newList.size < chatsList.size        ->
                    {
                        val removedPosition = firstMissingChatIndex(chatsList, newList) ?: return
                        chatsList.removeAt(removedPosition)
                        outerAdapter.notifyItemRemoved(removedPosition)
                    }
                    else                                 ->
                    {
                        val originalPosition = chatsList.indexOf(newList.firstOrNull())
                        if (originalPosition == -1)
                        {
                            Log.d(MainFireBaseAdapter.TAG, "moved chat is not in list")
                            return
                        }
                        val movedChat = newList.firstOrNull()!!
                        chatsList.removeAt(originalPosition)
                        chatsList.add(0, movedChat)
                        outerAdapter.notifyItemMoved(originalPosition, 0)
                        outerAdapter.notifyItemChanged(0)
                    }
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return object: RecyclerView.ViewHolder(view)
                {
                    //implemented because it must return something, this value is never used
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                          position: Int,
                                          card: Chat)
            {
            } //do nothing

            /***
             * assumes the lists have identical order and returns the first chat and its index that
             * is in l1 but not in l2
             */
            private fun firstMissingChatIndex(l1: List<Chat>, l2: List<Chat>): Int?
            {
                l1.forEachIndexed { i, chat ->
                    if (chat != l2[i]) return i
                }
                return null
            }
        }
    }
}