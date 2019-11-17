package com.technion.vedibarta.chatRoom

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : VedibartaActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val chatRoomListeners = ChatRoomListeners(this@ChatRoomActivity)

        setToolbar(chatToolbar)
        chatRoomListeners.configureListeners()
        val messageList = ArrayList<Message>()
        populateMessageList(messageList)// TODO remove after testing
        configureAdapter(messageList)
    }

    //TODO remove after testing
    private fun populateMessageList(messageList: ArrayList<Message>)
    {
        val m1 = Message("hello")
        val m2 = Message("Bye")
        m2.userId = 1

        messageList.add(m1)
        messageList.add(m2)
    }

    private fun configureAdapter(messageList: ArrayList<Message>)
    {
        chatView.layoutManager = LinearLayoutManager(this)
        chatView.adapter = ChatRoomMessageAdapter(messageList){Unit}
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false) // if you want to to write your own title programmatically
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> TODO()
        }
        return true
    }
}
