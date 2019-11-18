package com.technion.vedibarta.chatRoom

import android.app.Activity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.ListenersSetter

class ChatRoomListeners(private val chatRoom: Activity, private val supportFragmentManager: FragmentManager) {

    fun configureListeners() {
        val popupMenu = chatRoom.findViewById<View>(R.id.popupMenu)
        val sendButton = chatRoom.findViewById<View>(R.id.buttonChatBoxSend)
        val setter = ListenersSetter()
        setter.setListener(popupMenu, this::showPopup)
        setter.setListener(sendButton,this::sendMessage)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(chatRoom, view)
        popup.inflate(R.menu.chat_room_popup_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.generateQuestion -> {
                        ChatRoomQuestionGeneratorDialog().show(
                            supportFragmentManager,
                            "QuestionGeneratorFragment"
                        )
                }
                R.id.reportAbuse -> {
                    ChatRoomAbuseReportDialog().show(
                        supportFragmentManager,
                        "ReportAbuseDialog"
                    )
                }
            }

            true
        }
        popup.show()
    }

    private fun sendMessage(v: View)
    {
        Toast.makeText(chatRoom, "Message Sent", Toast.LENGTH_SHORT).show()
    }

}