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
import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : VedibartaActivity() {

    private val logTag = "ChatHistory"
    private lateinit var adapter: FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        configureAdapter()

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                doMySearch(query)
            }
        }

        extendedFloatingActionButton.setOnClickListener {
            startActivity(Intent(this, ChatSearchActivity::class.java))
        }

        if (student == null)
        {
            database.students().userId().build().get().addOnSuccessListener {document ->
                student = document.toObject(Student::class.java)
                Log.d(logTag, "loaded student profile successfully")
            }?.addOnFailureListener {
                Log.d(logTag, "${it.message}, cause: ${it.cause?.message}")
            } ?: Log.d(logTag, "student profile not found")
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

    private fun configureAdapter()
    {
        val query = database.students().userId().chats().build()
        val options = FirestoreRecyclerOptions.Builder<ChatCard>()
            .setQuery(query,ChatCard::class.java)
            .build()
        val chatHistory = findViewById<RecyclerView>(R.id.chat_history)
        adapter = getAdapter(options)
        chatHistory.layoutManager = LinearLayoutManager(this)
        chatHistory.adapter = adapter
    }

    private class ViewHolder (view: View): RecyclerView.ViewHolder(view)
    {
        fun bind(card: ChatCard)
        {
            itemView.findViewById<TextView>(R.id.user_name).text = card.userName
            itemView.findViewById<TextView>(R.id.last_message).text = card.userName
            itemView.findViewById<TextView>(R.id.relative_timestamp).text = card.userName
        }
    }
    private fun getAdapter(options: FirestoreRecyclerOptions<ChatCard>): FirestoreRecyclerAdapter<ChatCard,RecyclerView.ViewHolder>
    {
        return object: FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>(options)
        {
            override fun onDataChanged() {
                super.onDataChanged()
                Log.d("wtf", "onDataChanged")
            }
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder
            {
                Log.d("wtf", "onCreateViewHolder")
                val userNameView = LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                Log.d("wtf", "inflated")
                return ViewHolder(userNameView)
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                card: ChatCard
            ) {
                Log.d("wtf", "onBindViewHolder")
                when(holder)
                {
                    is ViewHolder -> holder.bind(card)
                }
             }
        }
    }
}
