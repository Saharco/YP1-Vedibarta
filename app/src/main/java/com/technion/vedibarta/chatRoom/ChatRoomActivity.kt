package com.technion.vedibarta.chatRoom

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ChatRoomActivity : VedibartaActivity()
    , ChatRoomQuestionGeneratorDialog.QuestionGeneratorDialogListener
    , ChatRoomAbuseReportDialog.AbuseReportDialogListener {
    val systemSender = "-1"
    private lateinit var adapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    lateinit var chatId: String
    lateinit var partnerId: String
    private var numMessages = 0
    private var photoUrl: String? = null
    private var otherGender: Gender? = null

    private val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())

    companion object {
        private const val TAG = "Vedibarta/chat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        window.setBackgroundDrawableResource(R.drawable.bg_chat_1)

        val chatMetaData = intent.getSerializableExtra("chatData") as ChatMetadata

        val partnerName = chatMetaData.partnerName
        chatId = chatMetaData.chatId
        partnerId = chatMetaData.partnerId
        numMessages = chatMetaData.numMessages

        otherGender = chatMetaData.partnerGender
        photoUrl = chatMetaData.partnerPhotoUrl

        chatPartnerId = partnerId
        photoUrl ?: displayDefaultProfilePicture()

        setToolbar(chatToolbar)
        configureAdapter()
        buttonChatBoxSend.setOnClickListener { sendMessage(it) }
        popupMenu.setOnClickListener { showPopup(it) }
        if (student!!.gender == Gender.FEMALE)
            chatBox.hint =
                SpannableStringBuilder(resources.getString(R.string.chat_room_enter_message_f))
        toolbarUserName.text = partnerName
        Glide.with(applicationContext)
            .asBitmap()
            .load(photoUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    toolbarProfileImage.setImageBitmap(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    displayDefaultProfilePicture()
                }
            })

    }

    private fun displayDefaultProfilePicture() {
        when (otherGender) {
            null -> return
            Gender.MALE -> toolbarProfileImage.setImageResource(R.drawable.ic_photo_default_profile_man)
            Gender.FEMALE -> toolbarProfileImage.setImageResource(R.drawable.ic_photo_default_profile_girl)
            else -> Log.d(TAG, "other student is neither male nor female??")
        }
    }

    override fun onStart()
    {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop()
    {
        super.onStop()
        adapter.stopListening()

    }

    private fun configureAdapter() {
        val query =
            database
                .chats()
                .chatId(chatId)
                .messages()
                .build().orderBy("timestamp", Query.Direction.DESCENDING)

        val options =
            FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message::class.java)
                .build()

        adapter = ChatRoomAdapter(this, options, numMessages)
        adapter.notifyDataSetChanged() //
        chatView.adapter = adapter
        adapter.notifyDataSetChanged() //
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        chatView.layoutManager = layoutManager
        chatView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            chatView.scrollToPosition(0)
        }
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false) // if you want to to write your own title programmatically
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onBackPressed() {
        chatPartnerId = null // exiting chat: notify that there is no chat partner
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> TODO()
        }
        return true
    }

    override fun onQuestionclick(dialog: DialogFragment, v: View) {
        try {
            val question = (v as TextView).text
            Toast.makeText(this, question, Toast.LENGTH_SHORT).show()
        } catch (e: ClassCastException) {
            Log.d("QuestionGenerator", e.toString())
        }
    }

    override fun onAbuseTypeClick(dialog: DialogFragment) {
        TODO("need to decide what to do")
        //Toast.makeText(this, "abuse", Toast.LENGTH_SHORT).show()
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
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

    private fun sendMessage(v: View) {
        var text = chatBox.text.toString()
        if (text.replace(" ", "")
                .replace("\n", "")
                .replace("\t", "")
                .isEmpty()
        )
            return

        text = text.replace("[\n]+".toRegex(), "\n").trim()

        val lastMessageDate: Date? = adapter.snapshots.firstOrNull()?.timestamp
        val currentDate = Date(System.currentTimeMillis())
        if (lastMessageDate != null)
        {
            val timeGap = currentDate.time - lastMessageDate.time
            val dayGap =
                (dayFormatter.format(currentDate).toInt() - dayFormatter.format(lastMessageDate).toInt())

            if (TimeUnit.DAYS.convert(timeGap, TimeUnit.MILLISECONDS) >= 1 || dayGap >= 1)
            {
                write(dateFormatter.format(currentDate), true)
            }
        }
        else
        {
            write(dateFormatter.format(currentDate), true)
        }
        write(text, false)
        chatBox.setText("")
    }

    private fun write(text: String, isGeneratorMessage: Boolean)
    {
        val timeSent = Date(System.currentTimeMillis())
        var sender = userId!!
        if (isGeneratorMessage)
        {
            sender = systemSender
        }
        val path = database.chats()
                .chatId(chatId)
                .messages()
                .build()
        path.add(Message(sender, partnerId, text, timeSent))
            .addOnFailureListener {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
    }
}
