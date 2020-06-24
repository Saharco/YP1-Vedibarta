package com.technion.vedibarta.chatRoom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.ImageLoader
import com.technion.vedibarta.utilities.questionGenerator.QuestionGeneratorFactory
import com.technion.vedibarta.utilities.questionGenerator.QuestionGeneratorManager

/***
 * specific chat screen, contains the messages history between 2 users
 */
class ChatRoomActivity : VedibartaActivity(),
    ChatRoomAbuseReportDialog.AbuseReportDialogListener
{
    private lateinit var adapter: ChatRoomAdapter
    private lateinit var messageSender: MessageSender
    lateinit var chatId: String
    lateinit var partnerId: String
    private var numMessages = 0
    private var partnerName = ""
    private var photoUrl: String? = null
    private var otherGender: Gender? = null
    private var partnerHobbies: Array<String> = emptyArray()
    private var firstVisibleMessagePosition = 0
    private val systemSenderId = "-1"
    private lateinit var questionGenerator : QuestionGeneratorManager
    companion object
    {
        private const val TAG = "Vedibarta/chat"
    }

    fun getNumMessages(): Int = adapter.itemCount


    fun sendSystemMessage(text: String) = apply { messageSender.sendMessage(text, true) }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        window.setBackgroundDrawableResource(R.drawable.bg_chat_1)

        val chatMetaData = intent.getSerializableExtra("chatData") as ChatMetadata

        partnerName = chatMetaData.partnerName
        chatId = chatMetaData.chatId
        partnerId = chatMetaData.partnerId
        numMessages = chatMetaData.numMessages
        otherGender = chatMetaData.partnerGender
        photoUrl = chatMetaData.partnerPhotoUrl
        partnerHobbies = chatMetaData.partnerHobbies

        chatPartnerId = partnerId // used by cloud functions
        questionGenerator = QuestionGeneratorFactory(this, student!!.hobbies.toList(), partnerHobbies.toList()).getGenerator()

        setToolbar(chatToolbar)
        configureAdapter()
        configureMessageSender(adapter)
        buttonChatBoxSend.setOnClickListener { sendMessageFromChatBox(it) }
        popupMenu.setOnClickListener { showPopup(it) }

        if (student!!.gender == Gender.FEMALE)
            chatBox.hint = SpannableStringBuilder(resources.getString(R.string.chat_room_enter_message_f))

        toolbarUserName.text = partnerName
        ImageLoader().loadProfileImage(photoUrl, otherGender!!, toolbarProfileImage, applicationContext)
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

    override fun onBackPressed()
    {
        chatPartnerId = null // exiting chat: notify that there is no chat partner
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            android.R.id.home -> onBackPressed()
            else              -> TODO()
        }
        return true
    }

    override fun onAbuseTypeClick(dialog: DialogFragment)
    {
        TODO("need to decide what to do")
        //Toast.makeText(this, "abuse", Toast.LENGTH_SHORT).show()
    }

    private fun sendMessageFromChatBox(v: View)
    {
        var text = chatBox.text.toString()
        if (text.isBlank()) return

        text = text.replace("[\n]+".toRegex(), "\n").trim()
        messageSender.sendMessage(text, false)
        chatBox.setText("")
    }

    private fun configureAdapter()
    {
        val query = database.chats().chat(chatId).messages().build()
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options =
            FirestoreRecyclerOptions.Builder<Message>().setQuery(query, Message::class.java).build()

        val soundPlayer = SoundPlayer(this, numMessages)
        adapter = ChatRoomFireBaseAdapter(options, userId!!, systemSenderId, soundPlayer)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)

        chatView.adapter = adapter
        chatView.layoutManager = layoutManager
        chatView.addOnScrollListener(firstVisibleMessageTracker())
        chatRoomRootView.viewTreeObserver.addOnGlobalLayoutListener(scrollToBottomOnKeyboardOpening())
        adapter.registerAdapterDataObserver(automaticScroller())
        adapter.notifyDataSetChanged()
    }

    private fun configureMessageSender(adapter: ChatRoomAdapter)
    {
        val errorCallback = { e: Exception ->
            Log.d(TAG, "MessageSender: ${e.message}, cause: ${e.cause?.message}")
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        }

        messageSender = MessageSender(adapter,
                                      chatId,
                                      userId!!,
                                      partnerId,
                                      systemSenderId,
                                      errorCallback)
    }

    private fun setToolbar(tb: Toolbar)
    {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false) // if you want to to write your own title programmatically
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun showPopup(view: View)
    {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.chat_room_popup_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId)
            {
                R.id.generateQuestion ->
                {
                    ChatRoomQuestionGeneratorDialog.newInstance(questionGenerator, this)
                        .show(supportFragmentManager, "QuestionGeneratorFragment")
                }

                R.id.reportAbuse      ->
                {
                    ChatRoomAbuseReportDialog(partnerId, partnerName, photoUrl, otherGender!!).show( supportFragmentManager, "ReportAbuseDialog")
                }
            }

            true
        }
        popup.show()
    }

    private fun firstVisibleMessageTracker(): RecyclerView.OnScrollListener
    {
        return object : RecyclerView.OnScrollListener()
        {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) firstVisibleMessagePosition =
                    (chatView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                Log.d("wtf", "firstvisible = $firstVisibleMessagePosition")
            }
        }
    }

    private fun automaticScroller(): RecyclerView.AdapterDataObserver
    {
        return object : RecyclerView.AdapterDataObserver()
        {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int)
            {
                super.onItemRangeInserted(positionStart, itemCount)
                val firstVisiblePosition =
                    (chatView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (firstVisiblePosition <= 1) chatView.scrollToPosition(0)
                else
                {
                    chatView.scrollToPosition(firstVisiblePosition + 1)
                }
            }

            override fun onChanged()
            {
                super.onChanged()
                val firstVisiblePosition =
                    (chatView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                chatView.scrollToPosition(firstVisiblePosition)
            }
        }
    }

    private fun scrollToBottomOnKeyboardOpening(): ViewTreeObserver.OnGlobalLayoutListener
    {

        return object : ViewTreeObserver.OnGlobalLayoutListener
        {
            private var isKeyBoardVisible = false
            override fun onGlobalLayout()
            {
                val activityRootView: View = findViewById(R.id.chatRoomRootView)
                val heightDiff = activityRootView.rootView.height - activityRootView.height;
                if (heightDiff > dpToPx(this@ChatRoomActivity, 200f) && !isKeyBoardVisible)
                {
                    isKeyBoardVisible = true
                    if (firstVisibleMessagePosition == 0) chatView.scrollToPosition(0)
                }
                else if (heightDiff < dpToPx(this@ChatRoomActivity, 200f) && isKeyBoardVisible)
                {
                    isKeyBoardVisible = false
                }
            }

            private fun dpToPx(context: Context, valueInDp: Float): Float
            {
                val metrics = context.resources.displayMetrics;
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
            }
        }
    }

}
