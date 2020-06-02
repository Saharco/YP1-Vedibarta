package com.technion.vedibarta.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

/***
 * adapter wrapping for the FireBaseAdapter to be used by the RecyclerView of MainActivity
 */
class MainFireBaseAdapter(val userId: String?,
                          private val applicationContext: Context,
                          private val chatPartnersMap: HashMap<String, ChatMetadata>,
                          private val mainActivity: MainActivity,
                          recyclerView: RecyclerView,
                          options: FirestoreRecyclerOptions<Chat>) :
    MainAdapter(recyclerView)
{
    companion object
    {
        const val TAG = "MainAdapter"
    }

    private val chatsList: MutableList<Chat> = ArrayList()
    private val firestoreAdapter = getFireStoreAdapter(options, this)

    override fun startListening()
    {
        super.startListening()
        firestoreAdapter.startListening()
    }

    override fun stopListening()
    {
        super.stopListening()
        firestoreAdapter.stopListening()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val userNameView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
        return ViewHolder(userNameView, userId!!, applicationContext)
    }

    override fun getItemCount(): Int
    {
        return chatsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val chat = chatsList[position]
        when (holder)
        {
            is ViewHolder ->
            {
                var chatMeta = ChatMetadata()
                var partner: Student? = null
                database
                    .students()
                    .otherUserId(chat.getPartnerId(userId!!))
                    .build()
                    .get()
                    .addOnSuccessListener {
                        partner = it.toObject(Student::class.java)
                        chatMeta = ChatMetadata.create(chat, userId, partner)
                    }.addOnFailureListener {
                        Log.d(MainActivity.TAG,
                              "Binding threw: ${it.message}, cause: ${it.cause?.message}")
                        chatMeta = ChatMetadata.create(chat, userId, null)
                    }.addOnCompleteListener {
                        Log.d(MainActivity.TAG, "Binding chat with the following data: $chatMeta")
                        holder.bind(chatMeta)
                        registerChatForFiltering(chatMeta)

                        holder.view.setOnClickListener {
                            val i = Intent(mainActivity, ChatRoomActivity::class.java)
                            i.putExtra("chatData", chatMeta)
                            mainActivity.startActivity(i)
                        }
                    }
            }

        }
    }


    private fun registerChatForFiltering(chatMetadata: ChatMetadata)
    {
        chatPartnersMap[chatMetadata.partnerName] = chatMetadata
    }

    private fun getFireStoreAdapter(options: FirestoreRecyclerOptions<Chat>,
                                    mainAdapter: MainAdapter): FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>
    {
        return object : FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>(options)
        {
            // onDataChange should be called on every change and as such there shouldn't be more
            // then 1 change at a time to the list except for first initialization
            override fun onDataChanged()
            {
                super.onDataChanged()

                val newList = this.snapshots.sortedWith(
                        compareByDescending<Chat, Date?>(nullsLast()) { it.lastMessageTimestamp })
                when
                {
                    (newList.size - chatsList.size) == 1 ->
                    {
                        chatsList.add(0, newList.first())
                        mainAdapter.notifyItemInserted(0)
                    }
                    (newList.size - chatsList.size) > 1  ->
                    {
                        // first initialization of list
                        chatsList.addAll(newList)
                        mainAdapter.notifyItemInserted(0)
                    }
                    newList.size < chatsList.size        ->
                    {
                        val removedPosition = firstMissingChatIndex(chatsList, newList) ?: return
                        chatsList.removeAt(removedPosition)
                        mainAdapter.notifyItemRemoved(removedPosition)
                    }
                    else                                 ->
                    {
                        val originalPosition = chatsList.indexOf(newList.firstOrNull())
                        if (originalPosition == -1)
                        {
                            Log.d(TAG, "moved chat is not in list")
                            return
                        }
                        val movedChat = newList.firstOrNull()!!
                        chatsList.removeAt(originalPosition)
                        chatsList.add(0, movedChat)
                        mainAdapter.notifyItemMoved(originalPosition, 0)
                        mainAdapter.notifyItemChanged(0)
                    }
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            {
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return ViewHolder(userNameView, userId!!, applicationContext)
            } //implemented because it must return something, this value is never used

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