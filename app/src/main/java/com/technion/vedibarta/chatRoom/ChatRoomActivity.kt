package com.technion.vedibarta.chatRoom

import android.os.Bundle
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity

class ChatRoomActivity : VedibartaActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val chatRoomListeners = ChatRoomListeners(this@ChatRoomActivity)
        chatRoomListeners.configureListeners()
    }
}
