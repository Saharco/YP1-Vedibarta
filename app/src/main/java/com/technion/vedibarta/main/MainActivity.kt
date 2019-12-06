package com.technion.vedibarta.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*


class MainActivity : VedibartaActivity() {

    private val logTag = "ChatHistory"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupRecyclerView()

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
            database.getStudentProfile()?.addOnSuccessListener {document ->
                student = document.toObject(Student::class.java)
                Log.d(logTag, "loaded student profile successfully")
            }?.addOnFailureListener {
                Log.d(logTag, "${it.message}, cause: ${it.cause?.message}")
            } ?: Log.d(logTag, "student profile not found")
        }
    }

    private fun doMySearch(query: String) {
        Log.d("Yuval", "Searching for something...")
    }

    private fun setupRecyclerView() {
        val chatHistory = findViewById<RecyclerView>(R.id.chat_history)
        chatHistory.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        chatHistory.layoutManager = layoutManager
        val demoCards = ArrayList<ChatCard>()
        val date = "today"
        demoCards.add(ChatCard(0,"יובל", "Hello", date))
        demoCards.add(ChatCard(0,"Sahar", "Hello", date))
        demoCards.add(ChatCard(0,"Victor", "Hello", date))
        demoCards.add(ChatCard(0,"Gil", "Hello", date))
        demoCards.add(ChatCard(0,"Ron", "Hello", date))
        demoCards.add(ChatCard(0,"Or", "Hello", date))
        demoCards.add(ChatCard(0,"Samuel", "Hello", date))
        demoCards.add(ChatCard(0,"Yuval", "Hello", date))
        demoCards.add(ChatCard(0,"Sahar", "Hello", date))
        demoCards.add(ChatCard(0,"Victor", "Hello", date))
        demoCards.add(ChatCard(0,"Gil", "Hello", date))
        demoCards.add(ChatCard(0,"Ron", "Hello", date))
        demoCards.add(ChatCard(0,"Or", "Hello", date))
        demoCards.add(ChatCard(0,"Samuel", "Hello", date))

        val adapter = object : ChatHistoryAdapter(demoCards) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                holder.view.setOnClickListener {
                    startActivity(Intent(this@MainActivity, ChatRoomActivity::class.java))
                }
            }
        }
        chatHistory.adapter = adapter


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
}
