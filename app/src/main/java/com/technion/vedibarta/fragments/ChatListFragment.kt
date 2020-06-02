package com.technion.vedibarta.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.main.*
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat_list.*
import java.util.HashMap


private const val USERID = "userId"

class ChatListFragment : Fragment(), MainActivity.OnBackPressed {
    private lateinit var userId: String


    private val chatPartnersMap = HashMap<String, ChatMetadata>()
    private var mainAdapter: MainAdapter? = null
    private lateinit var searchAdapter: MainsSearchAdapter<String>

    companion object {
        const val TAG = "Vedibarta/chat-lobby"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_chat_list, container, false)
        userId = VedibartaActivity.userId!!
        val chatList = v.findViewById<RecyclerView>(R.id.chat_history)
        chatList.layoutManager = LinearLayoutManager(requireContext())

        searchAdapter = MainSearchByNameAdapter(
            requireActivity().applicationContext,
            chatPartnersMap,
            activity as MainActivity,
            chatList
        )
        mainAdapter = getMainAdapter(chatList)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureSearchView()
        toolbar.inflateMenu(R.menu.chat_list_menu)
        searchView.setMenuItem(toolbar.menu.findItem(R.id.search))
        updateUserToken()
        chatSearchFab.setOnClickListener {
            findNavController().navigate(R.id.action_chats_to_chatSearchActivity)
        }
    }

    override fun onStart()
    {
        super.onStart()
        if (mainAdapter!!.itemCount == 0)
        {
            if (VedibartaActivity.student!!.gender == Gender.FEMALE)
                emptyListMessage.text = resources.getString(R.string.empty_chat_list_message_f)

            emptyListMessage.visibility = View.VISIBLE
            chat_history.visibility = View.GONE
        }

        mainAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        searchView.closeSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainAdapter?.stopListening()
    }

    private fun configureSearchView() {
        // used by searchListener below
        val queryListener = object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i(TAG, "textSubmit: $query")
                VedibartaActivity.hideKeyboard(requireActivity())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null || newText.isEmpty()) searchAdapter.filter("")
                else searchAdapter.filter(newText)
                return false
            }
        }


        val searchListener = object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                searchAdapter.stopListening()
                mainAdapter?.startListening()
            }

            override fun onSearchViewShown() {
                mainAdapter?.stopListening()
                searchAdapter.startListening()
                searchAdapter.filter("")
                searchView.setOnQueryTextListener(queryListener)
            }
        }

        searchView.setOnSearchViewListener(searchListener)
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

    private fun updateUserToken() {
        FirebaseInstanceId.getInstance()
            .instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful or (task.result == null)) {
                    Log.d(TAG, "getInstanceId failed")
                    return@OnCompleteListener
                }
                val token = task.result!!.token
                Log.d(TAG, "Token is: $token")
                DatabaseVersioning.currentVersion.instance.collection("students").document(userId!!)
                    .update("tokens", FieldValue.arrayUnion(token))
            })
    }

    private fun getMainAdapter(recycler: RecyclerView): MainAdapter {
        val adapterQuery = database.chats().build().whereArrayContains("participantsId", userId!!)

        val options =
            FirestoreRecyclerOptions.Builder<Chat>().setQuery(adapterQuery, Chat::class.java)
                .build()

        val adapter = MainFireBaseAdapter(
            userId,
            requireActivity().applicationContext,
            chatPartnersMap,
            activity as MainActivity,
            recycler,
            options
        )

        adapter.registerAdapterDataObserver(onChatPopulate())
        return adapter
    }

    override fun onBackPressed(): Boolean {
        if (searchView.isSearchOpen) {
            searchView.closeSearch()
            return true
        }
        return false
    }

}
