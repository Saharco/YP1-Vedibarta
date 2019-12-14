package com.technion.vedibarta.utilities.services

import android.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class VedibartaMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = "vedibarta/messaging"
    }

    //TODO: need to implement the service logic

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        TODO()
    }

    private fun sendChatNotification(title:String, message:String, senderId:String,
                                     senderPhotoURL:String?, chatId:String) {
        TODO()
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0) // remove? <-
        TODO()
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        TODO()
    }
}