package com.technion.vedibarta.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.technion.vedibarta.ExtentionFunctions.getName
import com.technion.vedibarta.ExtentionFunctions.getPartnerId
import com.technion.vedibarta.ExtentionFunctions.getPhoto
import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : VedibartaActivity() {

    private val logTag = "ChatHistory"
    private lateinit var adapter: FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>

    companion object {
        const val TAG = "Vedibarta/chat-lobby"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Update user's tokens
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful || task.result == null) {
                    Log.d(TAG, "getInstanceId failed")
                    return@OnCompleteListener
                }
                val token = task.result!!.token
                Log.d(TAG, "Token is: $token")
                FirebaseFirestore.getInstance()
                    .collection("students")
                    .document(userId!!)
                    .update("tokens", FieldValue.arrayUnion(token))
            })

        configureAdapter()

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                doMySearch(query)
            }
        }

        extendedFloatingActionButton.setOnClickListener {
            startActivity(Intent(this, ChatSearchActivity::class.java))
        }

        if (student == null) {
            database.students().userId().build().get().addOnSuccessListener { document ->
                student = document.toObject(Student::class.java)
                Log.d(logTag, "loaded student profile successfully")
            }.addOnFailureListener {
                Log.d(logTag, "${it.message}, cause: ${it.cause?.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconifiedByDefault = false // Do not iconify the widget; expand it by default
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_user_profile ->
                startActivity(Intent(this, UserProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doMySearch(query: String) {
        Log.d("Yuval", "Searching for something...")
    }

    private fun configureAdapter() {
        val query = database.chats().build().whereArrayContains("participantsId", userId!!)
        val options = FirestoreRecyclerOptions.Builder<ChatCard>()
            .setQuery(query, ChatCard::class.java)
            .build()
        val chatHistory = findViewById<RecyclerView>(R.id.chat_history)
        adapter = getAdapter(options)
        chatHistory.layoutManager = LinearLayoutManager(this)
        chatHistory.adapter = adapter
    }

    private class ViewHolder(val view: View, val userId: String) : RecyclerView.ViewHolder(view) {
        fun bind(card: ChatCard) {
            Log.d("wtf", "bind")
            try {
                val partnerId = card.getPartnerId(userId)
                itemView.findViewById<TextView>(R.id.user_name).text = card.getName(partnerId)
                itemView.findViewById<TextView>(R.id.last_message).text = card.lastMessage
                itemView.findViewById<TextView>(R.id.relative_timestamp).text = card.relativeTime
                val photoUrl = card.getPhoto(partnerId)
                //TODO load profile image into user_picture in card layout
            } catch (e: Exception) {
                com.technion.vedibarta.utilities.error(e)
            }
        }
    }

    private fun getAdapter(options: FirestoreRecyclerOptions<ChatCard>): FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder> {
        return object : FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                Log.d("wtf", "onCreateViewHolder")
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return ViewHolder(userNameView, userId!!)
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                card: ChatCard
            ) {
                Log.d("wtf", "onBindViewHolder")
                when (holder) {
                    is ViewHolder -> {
                        holder.bind(card)
                        holder.view.setOnClickListener {
                            val partnerId = card.getPartnerId(userId!!)
                            val i = Intent(this@MainActivity, ChatRoomActivity::class.java)
                            i.putExtra("chatId", card.chatId)
                            i.putExtra("partnerId", partnerId)
                            i.putExtra("name", card.getName(partnerId))
                            i.putExtra("photoUrl", card.getPhoto(partnerId))
                            i.putExtra("numMessages", card.numMessages)
                            startActivity(i)
                        }
                    }
                }
            }
        }
    }
}
