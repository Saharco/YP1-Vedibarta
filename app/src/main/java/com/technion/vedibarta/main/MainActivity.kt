package com.technion.vedibarta.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.technion.vedibarta.ExtentionFunctions.getName
import com.technion.vedibarta.ExtentionFunctions.getPartnerId
import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : VedibartaActivity() {

    private val logTag = "ChatHistory"
    private var searchKeyword: String? = null
    private lateinit var adapter: FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>

    private val chatPartnersMap = HashMap<String, ChatMetadata>()

    companion object {
        const val TAG = "Vedibarta/chat-lobby"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // Update user's tokens
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful || task.result == null) {
                    Log.d(TAG, "getInstanceId failed")
                    return@OnCompleteListener
                }
                val token = task.result!!.token
                Log.d(TAG, "Token is: $token")
                FirebaseFirestore.getInstance()
                    .collection("students")
                    .document(userId!!)
                    .update("tokens", FieldValue.arrayUnion(token))
            })

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                doMySearch(query)
            }
        }

        configureAdapter()

        extendedFloatingActionButton.setOnClickListener {
            startActivity(Intent(this, ChatSearchActivity::class.java))
        }

        if (student == null) {
            database.students().userId().build().get().addOnSuccessListener { document ->
                student = document.toObject(Student::class.java)
                Log.d(logTag, "loaded student profile successfully")
            }.addOnFailureListener {
                Log.d(logTag, "${it.message}, cause: ${it.cause?.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        searchView.setMenuItem(menu.findItem(R.id.search))
        return true
    }


    override fun onBackPressed() {
        if (searchView.isSearchOpen)
            searchView.closeSearch()
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_user_profile ->
                startActivity(Intent(this, UserProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doMySearch(query: String) {
        Log.d(TAG, "Searching for something...")
        if (query.isNotEmpty())
            searchKeyword = query
    }

    private fun configureAdapter() {
        val adapterQuery = database.chats().build().whereArrayContains("participantsId", userId!!)
        val options = FirestoreRecyclerOptions.Builder<ChatCard>()
            .setQuery(adapterQuery, ChatCard::class.java)
            .build()
        val chatHistory = findViewById<RecyclerView>(R.id.chat_history)
        adapter = getAdapter(options)
        chatHistory.layoutManager = LinearLayoutManager(this)
        chatHistory.adapter = adapter
    }

    private class ViewHolder(val view: View, val userId: String, val context: Context) :
        RecyclerView.ViewHolder(view) {
        private fun getString(x: Int): String {
            return context.resources.getString(x)
        }

        private fun calcRelativeTime(time: Date): String {
            try {
                val current = Date(System.currentTimeMillis())
                val timeGap = current.time - time.time
                val hoursGap = TimeUnit.HOURS.convert(timeGap, TimeUnit.MILLISECONDS)
                when (hoursGap) {
                    in 0..1 -> return getString(R.string.just_now)
                    in 2..24 -> return "${getString(R.string.sent)} ${getString(R.string.before)} $hoursGap ${getString(
                        R.string.hours
                    )}"
                    in 2..168 -> return "${getString(R.string.sent)} ${getString(R.string.before)} ${hoursGap / 24} ${getString(
                        R.string.days
                    )}"
                    else -> return SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(time)
                }
            } catch (e: Exception) {
                com.technion.vedibarta.utilities.error(e, "calc")
            }
            return ""
        }

        fun bind(card: ChatCard, photoUrl: String? = null, otherGender: Gender = Gender.MALE) {
            try {
                val partnerId = card.getPartnerId(userId)
                itemView.findViewById<TextView>(R.id.user_name).text = card.getName(partnerId)
                itemView.findViewById<TextView>(R.id.last_message).text = card.lastMessage
                itemView.findViewById<TextView>(R.id.relative_timestamp).text =
                    calcRelativeTime(card.lastMessageTimestamp)
                val profilePicture = itemView.findViewById<ImageView>(R.id.user_picture)

                if (photoUrl == null)
                    displayDefaultProfilePicture(profilePicture, otherGender)
                else {

                    Glide.with(context)
                        .asBitmap()
                        .load(photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .listener(object : RequestListener<Bitmap> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                displayDefaultProfilePicture(profilePicture, otherGender)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                profilePicture.setImageBitmap(resource)
                                return false
                            }

                        })
                        .into(profilePicture)
                }

            } catch (e: Exception) {
                com.technion.vedibarta.utilities.error(e)
            }
        }

        private fun displayDefaultProfilePicture(v: ImageView, otherGender: Gender) {
            when (otherGender) {
                Gender.MALE -> v.setImageResource(R.drawable.ic_photo_default_profile_man)
                Gender.FEMALE -> v.setImageResource(R.drawable.ic_photo_default_profile_girl)
                else -> Log.d(TAG, "other student is neither male nor female??")
            }
        }
    }

    private fun getAdapter(options: FirestoreRecyclerOptions<ChatCard>): FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder> {
        return object : FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return ViewHolder(userNameView, userId!!, applicationContext)
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                card: ChatCard
            ) {
                when (holder) {
                    is ViewHolder -> {
                        Log.d(TAG, "student is: $student")
                        FirebaseFirestore.getInstance().collection("students")
                            .document(card.getPartnerId(student!!.name))
                            .get()
                            .addOnSuccessListener { otherStudent ->
                                val otherStudentPhotoUrl = otherStudent["photo"] as String?
                                val otherGender = if (otherStudent["gender"] as String == "MALE")
                                    Gender.MALE
                                else
                                    Gender.FEMALE

                                holder.bind(card, otherStudentPhotoUrl, otherGender)
                                val partnerId = card.getPartnerId(userId!!)
                                val chatMetadata = ChatMetadata(
                                    card.chat!!,
                                    partnerId,
                                    card.getName(partnerId),
                                    card.numMessages,
                                    card.lastMessage,
                                    card.lastMessageTimestamp,
                                    otherGender,
                                    otherStudentPhotoUrl
                                )

                                chatPartnersMap[chatMetadata.partnerName] = chatMetadata

                                holder.view.setOnClickListener {
                                    val i = Intent(this@MainActivity, ChatRoomActivity::class.java)
                                    i.putExtra("chatData", chatMetadata)
                                    startActivity(i)
                                }
                            }.addOnFailureListener {
                                holder.bind(card)
                                val partnerId = card.getPartnerId(userId!!)
                                val chatMetadata = ChatMetadata(
                                    card.chat!!,
                                    partnerId,
                                    card.getName(partnerId),
                                    card.numMessages,
                                    card.lastMessage,
                                    card.lastMessageTimestamp
                                )

                                chatPartnersMap[chatMetadata.partnerName] = chatMetadata

                                holder.view.setOnClickListener {
                                    val i = Intent(this@MainActivity, ChatRoomActivity::class.java)
                                    i.putExtra("chatData", chatMetadata)
                                    startActivity(i)
                                }
                            }
                    }

                }
            }
        }
    }
}
