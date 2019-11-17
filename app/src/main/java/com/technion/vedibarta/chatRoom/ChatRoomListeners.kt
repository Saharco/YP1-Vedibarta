package com.technion.vedibarta.chatRoom

import android.app.Activity
import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.ListenersBuilder

class ChatRoomListeners(private val context: Context) {
    fun configureListeners() {
        val popupMenu = (context as Activity).findViewById<View>(R.id.popupMenu)
        val sendButton = (context as Activity).findViewById<View>(R.id.buttonChatBoxSend)
        val lb = ListenersBuilder()
        lb.addListener(popupMenu, this::showPopup)
        lb.addListener(sendButton,this::sendMessage)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.chat_room_popup_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.generateQuestion -> {
                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                }
                R.id.reportAbuse -> {
                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                }
            }

            true
        }
        popup.show()
    }

    private fun sendMessage(v: View)
    {
        Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show()
    }

}