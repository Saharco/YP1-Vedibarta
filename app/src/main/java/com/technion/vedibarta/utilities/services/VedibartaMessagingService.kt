package com.technion.vedibarta.utilities.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.technion.vedibarta.POJOs.ActivityCode
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.getCircleBitmap
import java.util.concurrent.ExecutionException

/**
 * This is a *background service*.
 * These listeners are constantly running on the user's device and listen to cloud functions' payloads
 * that are sent to a user's FCM token.
 * The service also handles new tokens.
 *
 */
class VedibartaMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "Vedibarta/messaging"
        private const val NO_PICTURE = "none"
        private const val GROUP_KEY_CHAT = "Vedibarta/CHAT"
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
                val senderPhotoURL =
                    messageData.getValue(getString(R.string.payload_data_sender_photo))
                sendChatNotification(title, messageBody, senderId, senderPhotoURL, chatId)
            }
            else -> return
        }
    }

    private fun sendChatNotification(
        title: String, message: String, senderId: String,
        senderPhotoURL: String, chatId: String
    ) {
        Log.d(TAG, "#sendChatNotification: starting...")

        if (VedibartaActivity.chatPartnerId != null && VedibartaActivity.chatPartnerId == senderId) {
            Log.d(
                TAG,
                "#sendChatNotification: user is currently in an active chat with the sender - do not fire a notification"
            )
            return
        }

        Log.d(
            TAG,
            "sendChatNotification: user is not in an active chat with the sender. Building the notification"
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            getString(R.string.vedibarta_notification_channel_name)
        )
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.flags =
            (Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
                    and Intent.FLAG_ACTIVITY_CLEAR_TOP)

        notificationIntent.putExtra("CHAT_ID", chatId)
        notificationIntent.putExtra("OTHER_ID", senderId)
        notificationIntent.putExtra("IS_NOT_REFERENCED_FROM_LOBBY", true)
        notificationIntent.putExtra(
            VedibartaActivity.EXTRA_CHANGE_ACTIVITY,
            ActivityCode.ActivityChatRoom
        )

        // This intent will start when the user clicks the notification.
        // A pending intent is required because it's "lazy" - a regular intent is instantaneous
        // and requires a context. We wrap it in a pending intent
        val notificationPendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val systemResources = Resources.getSystem()
        val chatTitle =
            SpannableString("\u200f${getString(R.string.notification_chat_title_prefix)}$title\u200f")
        chatTitle.setSpan(
            StyleSpan(Typeface.BOLD),
            getString(R.string.notification_chat_title_prefix).length + 1,
            chatTitle.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // adding the notification's properties
        builder.setSmallIcon(R.drawable.ic_launcher)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            .setWhen(System.currentTimeMillis())
            .setContentTitle(chatTitle)
            .setContentText("\u200f$message\u200f")
//            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(notificationPendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(500, 500))
            .setLights(
                ContextCompat.getColor(
                    this, systemResources
                        .getIdentifier("config_defaultNotificationColor", "color", "android")
                ),
                resources.getInteger(
                    systemResources
                        .getIdentifier("config_defaultNotificationLedOn", "integer", "android")
                ),
                resources.getInteger(
                    systemResources
                        .getIdentifier("config_defaultNotificationLedOff", "integer", "android")
                )
            )
//            .setLights(resources.getColor(R.color.colorPrimary), 3000, 3000)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(VISIBILITY_PUBLIC)
            .setGroup(GROUP_KEY_CHAT)
            .setAutoCancel(true)

        if (senderPhotoURL != NO_PICTURE) {
            val futureTarget = Glide.with(this)
                .asBitmap()
                .load(senderPhotoURL)
                .submit()
            try {
                val bitmap = futureTarget.get()
                builder.setLargeIcon(getCircleBitmap(bitmap))
            } catch (e: InterruptedException) { // do nothing
            } catch (e: ExecutionException) { // do nothing
            }

            Glide.with(this).clear(futureTarget)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d(TAG, "sendChatNotification: firing notification")

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // on newer APIs - need to set the channel ID for our notification
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val notificationChannel = NotificationChannel(
                getString(R.string.vedibarta_notification_channel_name),
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            // configure the notification channel
            notificationChannel.description = message
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(sound, attributes)
            notificationManager.createNotificationChannel(
                notificationChannel
            )
        }

        // notification ID prevents spamming the device's notification tray -
        // notifications of the same ID will override one another
        // it's impossible to create a unique integer key from a string value.
        // i used the hash code value to generate a decent key in terms of uniqueness for each user
        notificationManager.notify(senderId.hashCode(), builder.build())
    }

    override fun onNewToken(token: String) {
//        super.onNewToken(token) <-- remove this?
        Log.d(TAG, "Refreshed user token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection("students").document(user.uid)
                .update("tokens", FieldValue.arrayUnion(token))
        }
    }
}