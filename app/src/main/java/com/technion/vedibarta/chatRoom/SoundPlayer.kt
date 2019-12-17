package com.technion.vedibarta.chatRoom

import android.app.Activity
import android.media.MediaPlayer
import android.os.Handler
import com.technion.vedibarta.R
import java.lang.Exception

class SoundPlayer(private val activity: Activity, private var numMessages: Int) {
    private val MESSAGE_SOUND_INTERVAL: Long = 2000
    private val soundHandler = Handler()
    private val soundTask = Runnable { soundHandler.removeMessages(0) }
    var sendPlayer = MediaPlayer.create(activity, R.raw.message_sent_audio)
    var receivePlayer = MediaPlayer.create(activity, R.raw.message_received_audio)

    fun release() {
        sendPlayer.stop()
        receivePlayer.stop()
        sendPlayer.release()
        receivePlayer.release()
    }

    fun init() {
        sendPlayer = MediaPlayer.create(activity, R.raw.message_sent_audio)
        receivePlayer = MediaPlayer.create(activity, R.raw.message_received_audio)
    }

    fun playMessageSound(type: MessageType, count: Int): Boolean {
        var res = false
        if (!soundHandler.hasMessages(0)) {
            soundHandler.removeCallbacksAndMessages(null)
            soundHandler.postDelayed(soundTask, MESSAGE_SOUND_INTERVAL)
            try {
                res = tryToPlaySound(type, count)
            } catch (e: Exception) {
                com.technion.vedibarta.utilities.error(e, "tryToPlaySound")
            }
        }
        return res
    }

    private fun tryToPlaySound(type: MessageType, count: Int): Boolean {
        if (count > numMessages) {
            // Update the amount of already-acknowledged messages
            numMessages = count
            // Let the handler know about the successful sound invocation, so it can lock it for a set time
            soundHandler.sendEmptyMessage(0)

            if (type == MessageType.USER) {
                if (!sendPlayer.isPlaying)
                    sendPlayer.start()
            } else {
                if (!receivePlayer.isPlaying)
                    receivePlayer.start()
            }
            return true

        }
        return false
    }
}