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
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.data.loadHobbies
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/***
 * main screen of the app, contains the chat history/list
 */
class MainActivity : VedibartaActivity()
{
    private val chatPartnersMap = HashMap<String, ChatMetadata>()
    private lateinit var mainAdapter: MainAdapter
    private lateinit var searchAdapter: MainsSearchAdapter<String>

    companion object
    {
        const val TAG = "Vedibarta/chat-lobby"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        chat_history.layoutManager = LinearLayoutManager(this)
        configureSearchView()

        extendedFloatingActionButton.setOnClickListener {
            startActivity(Intent(this, ChatSearchActivity::class.java))
        }

        updateUserToken()

        //must be constructed after applicationContext is initialized
        searchAdapter = MainSearchByNameAdapter(applicationContext, chatPartnersMap, this)
        mainAdapter = getMainAdapter()

        //ResourceDownload
        RemoteResourcesManager(this)
            .findMultilingualResource("hobbies/all")

        loadHobbies(this)
    }

    override fun onStart()
    {
        super.onStart()
        if (mainAdapter.itemCount == 0)
        {
            if (student!!.gender == Gender.FEMALE)
                emptyListMessage.text = resources.getString(R.string.empty_chat_list_message_f)

            emptyListMessage.visibility = View.VISIBLE
            chat_history.visibility = View.GONE
        }

        mainAdapter.startListening()
    }

    override fun onStop()
    {
        super.onStop()
        searchView.closeSearch()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mainAdapter.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        searchView.setMenuItem(menu.findItem(R.id.search))
        return true
    }

    override fun onBackPressed()
    {
        if (searchView.isSearchOpen) searchView.closeSearch()
        else super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.action_user_profile -> startActivity(Intent(this, UserProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUserToken()
    {
        FirebaseInstanceId.getInstance()
            .instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful or (task.result == null))
            {
                Log.d(TAG, "getInstanceId failed")
                return@OnCompleteListener
            }
            val token = task.result!!.token
            Log.d(TAG, "Token is: $token")
            DatabaseVersioning.currentVersion.instance.collection("students").document(userId!!)
                .update("tokens", FieldValue.arrayUnion(token))
        })
    }

    private fun configureSearchView()
    {
        // used by searchListener below
        val queryListener = object : MaterialSearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String?): Boolean
            {
                Log.i(TAG, "textSubmit: $query")
                hideKeyboard(this@MainActivity)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean
            {
                if (newText == null || newText.isEmpty()) searchAdapter.filter("")
                else searchAdapter.filter(newText)
                return false
            }
        }


        val searchListener = object : MaterialSearchView.SearchViewListener
        {
            override fun onSearchViewClosed()
            {
                searchAdapter.stopListening()
                mainAdapter.startListening()
            }

            override fun onSearchViewShown()
            {
                mainAdapter.stopListening()
                searchAdapter.startListening()
                searchAdapter.filter("")
                searchView.setOnQueryTextListener(queryListener)
            }
        }

        searchView.setOnSearchViewListener(searchListener)
    }

    private fun onChatPopulate(): RecyclerView.AdapterDataObserver
    {
        return object : RecyclerView.AdapterDataObserver()
        {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int)
            {
                super.onItemRangeInserted(positionStart, itemCount)
                emptyListMessage.visibility = View.GONE
                chat_history.visibility = View.VISIBLE
            }
        }
    }

    private fun getMainAdapter(): MainAdapter
    {
        val adapterQuery = database.chats().build().whereArrayContains("participantsId", userId!!)

        val options =
            FirestoreRecyclerOptions.Builder<Chat>().setQuery(adapterQuery, Chat::class.java)
                .build()

        val adapter = MainFireBaseAdapter(userId,
                                          applicationContext,
                                          chatPartnersMap,
                                          this@MainActivity,
                                          options)

        adapter.registerAdapterDataObserver(onChatPopulate())
        return adapter
    }
}
