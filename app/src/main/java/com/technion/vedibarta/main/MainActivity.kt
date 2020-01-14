package com.technion.vedibarta.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.iid.FirebaseInstanceId
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : VedibartaActivity() {

    private lateinit var mainAdapter: MainAdapter
    private lateinit var searchAdapter: RecyclerView.Adapter<ViewHolder>

    private val chatPartnersMap = HashMap<String, ArrayList<ChatMetadata>>()

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
                DatabaseVersioning.currentVersion.instance
                    .collection("students")
                    .document(userId!!)
                    .update("tokens", FieldValue.arrayUnion(token))
            })

        extendedFloatingActionButton.setOnClickListener {
            startActivity(Intent(this, ChatSearchActivity::class.java))
        }

        configureSearchView()
    }

    private fun configureSearchView() {
        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                // do nothing
            }

            override fun onSearchViewShown() {
                searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        Log.i(TAG, "textSubmit: $query")
                        hideKeyboard(this@MainActivity)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText == null || newText == "") {
                            showAllChats()
                        } else
                            showFilteredChats(newText)
                        return false
                    }
                })
            }
        })
    }

    private fun showFilteredChats(query: String) {
        Log.d(TAG, "changing to filtered query")

        mainAdapter.firestoreAdapter.stopListening()

        val filteredList = chatPartnersMap.filterKeys {
            it.startsWith(
                query,
                ignoreCase = true
            ) || it.split(" ")[1].startsWith(query, ignoreCase = true)
        }.values
            .flatten()
            .sortedBy { it.lastMessageTimestamp } // observe that that Date implements Comparable!

        searchAdapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return ViewHolder(userNameView, userId!!, applicationContext)
            }

            override fun getItemCount(): Int {
                return filteredList.size
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val chatMetadata = filteredList[holder.adapterPosition]
                Log.d(TAG, "Binding chat with the following data: $chatMetadata")
                holder.bind(chatMetadata)
                holder.view.setOnClickListener {
                    val intent = Intent(this@MainActivity, ChatRoomActivity::class.java)
                    intent.putExtra("chatData", chatMetadata)
                    startActivity(intent)
                }
            }
        }
        chat_history.layoutManager = LinearLayoutManager(this)
        chat_history.adapter = searchAdapter
    }

    private fun showAllChats() {
        Log.d(TAG, "showing all chat results")

        mainAdapter = getMainAdapter()
        mainAdapter.registerAdapterDataObserver(onChatPopulate())
        chat_history.layoutManager = LinearLayoutManager(this)
        chat_history.adapter = mainAdapter
        mainAdapter.firestoreAdapter.startListening()
    }


    override fun onStart() {
        super.onStart()
        if (student!!.gender == Gender.FEMALE)
            emptyListMessage.text = resources.getString(R.string.empty_chat_list_message_f)
        emptyListMessage.visibility = View.VISIBLE
        chat_history.visibility = View.GONE
        showAllChats()
        mainAdapter.firestoreAdapter.startListening()
    }

    private fun onChatPopulate(): RecyclerView.AdapterDataObserver {
        return object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                emptyListMessage.visibility = View.GONE
                chat_history.visibility = View.VISIBLE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        searchView.closeSearch()
        mainAdapter.firestoreAdapter.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        searchView.setMenuItem(menu.findItem(R.id.search))
        return true
    }

    override fun onBackPressed() {
        if (searchView.isSearchOpen)
            searchView.closeSearch()
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_user_profile ->
                startActivity(Intent(this, UserProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getMainAdapter(): MainAdapter {
        val adapterQuery = database
            .chats()
            .build()
            .whereArrayContains("participantsId", userId!!)

        val options = FirestoreRecyclerOptions.Builder<Chat>()
            .setQuery(adapterQuery, Chat::class.java)
            .build()

        return MainAdapter(userId, applicationContext, chatPartnersMap, this@MainActivity, options)
    }
}
