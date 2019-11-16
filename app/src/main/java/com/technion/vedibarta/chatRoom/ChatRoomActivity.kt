package com.technion.vedibarta.chatRoom

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : VedibartaActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val chatRoomListeners = ChatRoomListeners(this@ChatRoomActivity)
        chatRoomListeners.configureListeners()
        setToolbar(chatToolbar)
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
