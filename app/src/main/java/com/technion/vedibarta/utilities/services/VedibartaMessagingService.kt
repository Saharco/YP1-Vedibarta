package com.technion.vedibarta.utilities.services

import android.util.Log
import com.technion.vedibarta.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * This background service is constantly running on the user's device and listens to cloud functions' payloads
 * that are sent to a user's FCM token.
 * The service also handles new new tokens.
 *
 * //FIXME: this class is not yet complete
 */
class VedibartaMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = "vedibarta/messaging"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "#onMessageReceived: started with message: ${remoteMessage.data}")
        val payloadDisplayStatus =
            remoteMessage.data[getString(R.string.payload_display_status)] ?: return

        if (payloadDisplayStatus == getString(R.string.payload_admin_broadcast)) {
            Log.d(TAG, "#onMessageReceived: notification is an admin notification")
            buildAdminBroadcastNotification(remoteMessage.data)
        }
    }

    private fun buildAdminBroadcastNotification(messageData: Map<String, String>) {
        val notificationType =
            messageData[getString(R.string.payload_data_notification_type)] ?: return
        val title = messageData.getValue(getString(R.string.payload_data_message_title))
        val messageBody = messageData.getValue(getString(R.string.payload_data_message_body))

        when (notificationType) {
            // possibly add more notification types in the future. for now, there's only chat
            "CHAT" -> {
                val senderId = messageData.getValue(getString(R.string.payload_data_sender_id))
                val chatId = messageData.getValue(getString(R.string.payload_data_chat_id))
                val senderPhotoURL = messageData.getValue(getString(R.string.payload_data_sender_photo))
                sendChatNotification(title, messageBody, senderId, senderPhotoURL, chatId)
            }
            else -> return
        }
    }

    private fun sendChatNotification(
        title: String, message: String, senderId: String,
        senderPhotoURL: String?, chatId: String
    ) {
        Log.d(TAG, "#sendChatNotification: starting...")
        Log.d(TAG, "#sendChatNotification: checking if the user is currently in an active chat with the sender")


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