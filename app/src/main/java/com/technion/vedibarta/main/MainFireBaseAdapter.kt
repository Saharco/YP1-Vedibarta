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
import com.technion.vedibarta.dagger.DaggerMainInnerAdapterInjector
import com.technion.vedibarta.dagger.MainInnerAdapterModule
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap
import javax.inject.Inject

/***
 * adapter wrapping for the FireBaseAdapter to be used by the RecyclerView of MainActivity
 */
class MainFireBaseAdapter(val userId: String?,
                          private val applicationContext: Context,
                          private val chatPartnersMap: HashMap<String, ChatMetadata>,
                          private val mainActivity: MainActivity,
                          options: FirestoreRecyclerOptions<Chat>) :
    MainAdapter(mainActivity.chat_history)
{
    companion object
    {
        const val TAG = "MainAdapter"
    }

    private val database = mainActivity.database
    private val chatsList: MutableList<Chat> = ArrayList()
    @Inject lateinit var firestoreAdapter: FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>

    init
    {
        DaggerMainInnerAdapterInjector.builder()
                .mainInnerAdapterModule(MainInnerAdapterModule(this, chatsList, options))
                .build()
                .inject(this)
    }

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
        return ViewHolder(userNameView, userId!!, applicationContext, database)
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
}