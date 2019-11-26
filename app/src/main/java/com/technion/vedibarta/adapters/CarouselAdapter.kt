package com.technion.vedibarta.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.dpToPx
import kotlinx.android.synthetic.main.activity_user_profile.*

class CarouselAdapter(val context: Context, val itemClick: (position: Int, carouselAdapterItem: Student) -> Unit) :
    RecyclerView.Adapter<ItemViewHolder>() {

    private val TAG = "carouselAdapter"

    private var carouselAdapterItems: List<Student> = listOf()
    private val selectedPos = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item_horizontal,
                    parent,
                    false
                ))
        } else {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item,
                    parent,
                    false
                ))
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(carouselAdapterItems[position])
        holder.itemView.setOnClickListener {
            itemClick(position, carouselAdapterItems[position])
        }
        holder.itemView.isSelected = position == selectedPos

        val student = carouselAdapterItems[position]
        loadProfilePicture(holder.profilePicture, student)
        holder.name.text = student.name
        holder.description.text = "${student.school}, ${student.region}"
        populateTable(holder.table, student)
    }

    private fun loadProfilePicture(profilePicture: ImageView, student: Student) {
        if (student.photo == null) {
            loadDefaultUserProfilePicture(profilePicture, student)
            return
        }

        Glide.with(context)
            .asBitmap()
            .load(student.photo)
            .apply(RequestOptions.circleCropTransform())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadDefaultUserProfilePicture(profilePicture, student)
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
//                    displayUserProfilePicture(profilePicture)
                    return false
                }

            })
            .into(profilePicture)
    }

    private fun loadDefaultUserProfilePicture(profilePicture: ImageView, student: Student) {
        if (student.gender == Gender.MALE)
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_man)
        else
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_girl)
//        displayUserProfilePicture(profilePicture)
    }

    @SuppressLint("InflateParams")
    private fun populateTable(table: TableLayout, student: Student) {

        val tableRowParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableRowParams.topMargin = 40 // in pixels

        val bubbleParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (student.characteristics.isEmpty()) {
            handleNoCharacteristics()
            return
        }

        Log.d(TAG, "Table width (in pixels): ${dpToPx(context.resources, 184f)}")

        val steps = calculateBubblesInRow(table)

        Log.d(TAG, "Amount of bubbles in a row: $steps")

        (student.characteristics.indices step steps).forEach { i ->
            val tableRow = TableRow(context)
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            for (j in 0 until steps) {
                if (i + j >= student.characteristics.size)
                    break

                val bubbleFrame = LayoutInflater.from(context).inflate(
                    R.layout.user_profile_bubble_blue,
                    null
                ) as FrameLayout

                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = student.characteristics[i + j]
                bubbleFrame.layoutParams = bubbleParams
                bubbleFrame.scaleX = 0.25f
                bubbleFrame.scaleY = 0.25f

                tableRow.addView(bubbleFrame)
            }

            table.addView(tableRow)
        }
    }

    private fun calculateBubblesInRow(table: TableLayout): Int =
        ((dpToPx(context.resources, 184f)) / (dpToPx(context.resources, 25f))).toInt()

    private fun handleNoCharacteristics() {
        //TODO: add some behavior for the scenario where the user has no characteristics
    }


    override fun getItemCount() = carouselAdapterItems.size

    fun setItems(newCarouselAdapterItems: List<Student>) {
        carouselAdapterItems = newCarouselAdapterItems
        notifyDataSetChanged()
    }
}

class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    val profilePicture: ImageView = view.findViewById(R.id.candidateProfilePicture)
    val name : TextView = view.findViewById(R.id.candidateName)
    val description : TextView = view.findViewById(R.id.candidateDescription)
    val table : TableLayout = view.findViewById(R.id.candidateTable)
    val button : Button = view.findViewById(R.id.confirmCandidateButton)

    fun bind(carouselAdapterItem: Student) {
        //TODO: add listeners, tags, etc here
    }
}